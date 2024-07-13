// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.util.functional;

import de.uni_passau.fim.se2.sa.ggnn.util.dotgraph.DotGraph;
import de.uni_passau.fim.se2.sa.ggnn.util.dotgraph.DotGraphEdge;
import de.uni_passau.fim.se2.sa.ggnn.util.dotgraph.DotGraphable;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.AsUnmodifiableGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractGraph<N> implements DotGraphable<N> {

    protected final String graphName;
    protected final Set<N> labelNodes;
    protected final Graph<N, DefaultEdge> graph;
    protected final Function<N, String> labelGenerator;

    protected AbstractGraph(
        String graphName, Set<N> labelNodes, Graph<N, DefaultEdge> graph, Function<N, String> labelGenerator
    ) {
        this.graph = new AsUnmodifiableGraph<>(graph);
        this.graphName = graphName;
        this.labelNodes = Collections.unmodifiableSet(labelNodes);
        this.labelGenerator = labelGenerator;
    }

    public Graph<N, DefaultEdge> graph() {
        return graph;
    }

    public Set<N> labelNodes() {
        return labelNodes;
    }

    public String name() {
        return graphName;
    }

    /**
     * Generate an empty graph base.
     *
     * @param <N> the node type
     *
     * @return Returns an empty graph.
     */
    public static <N> Graph<N, DefaultEdge> emptyBaseGraph() {
        return GraphTypeBuilder
            .<N, DefaultEdge>directed()
            .allowingMultipleEdges(true)
            .allowingSelfLoops(true)
            .edgeClass(DefaultEdge.class)
            .weighted(false)
            .buildGraph();
    }

    /**
     * Converts the graph into the dot graph representation.
     *
     * @return A dot graph representing this control flow graph.
     */
    @Override
    public DotGraph<N> asDotGraph() {
        final List<DotGraphEdge<N>> edges = graph
            .edgeSet()
            .stream()
            .map(edge -> Pair.of(graph.getEdgeSource(edge), graph.getEdgeTarget(edge)))
            .map(DotGraphEdge::new)
            .toList();
        return new DotGraph<>(graphName, edges, Collections.emptyList(), this.labelGenerator);
    }

    /**
     * Gets the immediate predecessors of the given node.
     *
     * @param node the node to output the predecessors for.
     * @return a Set of immediate predecessors of the given node.
     */
    public Set<N> getImmediatePredecessors(final N node) {
        return Set.copyOf(Graphs.predecessorListOf(graph, node));
    }

    /**
     * Gets the immediate successors of the given node.
     *
     * @param node the node to get the immediate successors for,
     * @return a set of immediate successors.
     */
    public Set<N> getImmediateSuccessors(final N node) {
        if (!graph.containsVertex(node)) {
            return Collections.emptySet();
        }
        return Set.copyOf(Graphs.successorListOf(graph, node));
    }

    /**
     * Gets the transitive successors of the given node.
     *
     * @param node The given node.
     * @return The Set of transitive successors of the given node.
     */
    public Set<N> getTransitiveSuccessors(final N node) {
        final Set<N> successors = new HashSet<>();
        final GraphIterator<N, DefaultEdge> iterator = new BreadthFirstIterator<>(graph, node);
        while (iterator.hasNext()) {
            successors.add(iterator.next());
        }
        return successors;
    }

    /**
     * Checks if the transitive successor set of the start node contains all other nodes.
     * <p>
     * The start node is seen as a transitive successor of itself (path length zero).
     *
     * @param startNode The start node.
     * @param nodes     Some other nodes.
     * @return If the transitive successors of the start node contains all other nodes.
     */
    @SafeVarargs
    public final boolean containsTransitiveSuccessors(final N startNode, final N... nodes) {
        final Set<N> transitiveSuccessors = getTransitiveSuccessors(startNode);
        transitiveSuccessors.add(startNode);
        return Arrays.stream(nodes).allMatch(transitiveSuccessors::contains);
    }

    /**
     * Checks if the target node is a successor of the source node (or the source node itself) and therefore reachable
     * from the source node.
     *
     * @param source The source node.
     * @param target The target node.
     * @return If the target node is reachable by the source node.
     */
    public boolean isReachable(final N source, final N target) {
        return containsTransitiveSuccessors(source, target);
    }

    public Set<Pair<N, N>> getEdges() {
        return graph.edgeSet().stream()
            .map(e -> Pair.of(graph.getEdgeSource(e), graph.getEdgeTarget(e)))
            .collect(Collectors.toSet());
    }

    public Set<N> getVertices() {
        return graph.vertexSet();
    }
}
