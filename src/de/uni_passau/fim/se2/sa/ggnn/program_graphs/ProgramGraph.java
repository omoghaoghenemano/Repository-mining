// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.cfg.CfgBuilder;
import de.uni_passau.fim.se2.sa.ggnn.util.dotgraph.AstNodeLabelGenerator;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.AbstractGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Collections;
import java.util.Optional;

/**
 * The control flow graph of a method. Can be constructed using the {@link CfgBuilder}.
 */
public abstract class ProgramGraph<T extends ProgramGraph<T>> extends AbstractGraph<PGNode> {

    private final PGNode entryNode;
    private final PGNode exitNode;

    protected ProgramGraph(String graphName, Graph<PGNode, DefaultEdge> graph) {
        super(graphName, Collections.emptySet(), graph, ProgramGraph::labelGenerator);

        entryNode = findNode(PGNode.ENTRY_NODE_LABEL).orElseThrow();
        exitNode = findNode(PGNode.EXIT_NODE_LABEL).orElseThrow();
    }

    protected Optional<PGNode> findNode(final AstNode astNode) {
        return graph.vertexSet()
            .stream()
            .filter(node -> astNode.equals(node.node()))
            .findFirst();
    }

    public PGNode entryNode() {
        return entryNode;
    }

    public PGNode exitNode() {
        return exitNode;
    }

    /**
     * Reverses the graph without swapping the entry and exit nodes.
     * <p>
     * This allows the re-use of the existing graph without copying and turns the reversal into a near zero-cost
     * operation.
     *
     * <h3>Implementation note</h3>
     *
     * This should usually be implemented as {@code new ConcreteGraph(graphName, new EdgeReversedGraph(graph)}.
     *
     * @return A reversed graph except that the entry and exit nodes have now swapped roles.
     */
    public abstract T reversedEntryExitNotSwapped();

    /**
     * Fully reverses the graph including the entry and exit nodes.
     * <p>
     * This operation requires rebuilding the graph with all nodes and edges. Therefore, if possible, the cheaper
     * operation {@link #reversedEntryExitNotSwapped()} should be used instead.
     *
     * @return A reversed graph.
     */
    protected Graph<PGNode, DefaultEdge> reversedGraph() {
        final Graph<PGNode, DefaultEdge> newGraph = emptyBaseGraph();

        final PGNode newEntry = new PGNode(PGNode.ENTRY_NODE_LABEL);
        newGraph.addVertex(newEntry);
        final PGNode newExit = new PGNode(PGNode.EXIT_NODE_LABEL);
        newGraph.addVertex(newExit);

        graph().vertexSet()
            .stream()
            .filter(c -> !c.equals(entryNode) && !c.equals(exitNode))
            .forEach(newGraph::addVertex);

        graph().edgeSet().forEach(edge -> {
            final PGNode source = graph.getEdgeSource(edge);
            final PGNode target = graph.getEdgeTarget(edge);

            if (source.equals(entryNode)) {
                newGraph.addEdge(target, newExit);
            }
            else if (target.equals(exitNode)) {
                newGraph.addEdge(newEntry, source);
            }
            else {
                newGraph.addEdge(target, source);
            }
        });

        return newGraph;
    }

    /**
     * Fully reverses the graph including the entry and exit nodes.
     * <p>
     * This operation requires rebuilding the graph with all nodes and edges. Therefore, if possible, the cheaper
     * operation {@link #reversedEntryExitNotSwapped()} should be used instead.
     *
     * <h3>Implementation note</h3>
     *
     * This should usually be implemented as {@code new ConcreteGraph(graphName, reversedGraph()}.
     *
     * @return A reversed graph.
     */
    public abstract T reversed();

    private static String labelGenerator(final PGNode node) {
        return AstNodeLabelGenerator.getLabel(node.node());
    }
}
