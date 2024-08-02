// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors.GGNNEdgesVisitor;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.Builder;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Map;
import java.util.Set;

/**
 * Builder of the GGNN Graph.
 */
public class GGNNGraphBuilder implements Builder<MethodDeclaration, GGNNGraph> {

    /**
     * Builds the GGNN graph by first defining all GGNN-Edges using the {@link GGNNEdgesVisitor}.
     * Given the GGNN-Edges the GGNN graph is built by adding all required nodes and linking them accordingly.
     *
     * @param method based on which the GGNN will be built.
     * @return the GGNN graph.
     */
    @Override
    public GGNNGraph build(final MethodDeclaration method) {
        // Instantiate the GGNNEdgesVisitor
        GGNNEdgesVisitor edgesVisitor = new GGNNEdgesVisitor(method);
        // Get the edges from the visitor
        Map<GGNNEdgeType, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>> edgeTypeSetMap = edgesVisitor.getEdges();

        // Create the directed graph
        Graph<IdentityWrapper<AstNode>, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        // Add all vertices (nodes) to the graph
        edgeTypeSetMap.values().forEach(pairs -> {
            pairs.forEach(pair -> {
                graph.addVertex(pair.a());
                graph.addVertex(pair.b());
            });
        });

        // Add all edges to the graph
        edgeTypeSetMap.values().forEach(pairs -> {
            pairs.forEach(pair -> {
                graph.addEdge(pair.a(), pair.b());
            });
        });

        // Get the label nodes (in this case, it's just the method name)
        Set<IdentityWrapper<AstNode>> labelNodes = getLabelNodes(method);

        // Create and return the GGNNGraph
        return new GGNNGraph(method.name().toString(), labelNodes, graph, edgeTypeSetMap);
    }


    private Set<IdentityWrapper<AstNode>> getLabelNodes(final MethodDeclaration method) {
        return Set.of(IdentityWrapper.of(method.name()));
    }
}
