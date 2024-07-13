// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.util.dotgraph.AstNodeLabelGenerator;
import de.uni_passau.fim.se2.sa.ggnn.util.dotgraph.DotGraph;
import de.uni_passau.fim.se2.sa.ggnn.util.dotgraph.DotGraphEdge;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.AbstractGraph;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

/**
 * Implementation of the GGNN Graph.
 */
public class GGNNGraph extends AbstractGraph<IdentityWrapper<AstNode>> {

    /**
     * Edge type and its corresponding edges.
     */
    private final Map<GGNNEdgeType, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>> edgeTypeSetMap;

    protected GGNNGraph(
        String graphName,
        Set<IdentityWrapper<AstNode>> labelNodes,
        Graph<IdentityWrapper<AstNode>, DefaultEdge> graph,
        Map<GGNNEdgeType, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>> edgeTypeSetMap
    ) {
        super(graphName, labelNodes, graph, n -> AstNodeLabelGenerator.getLabel(n.elem()));
        this.edgeTypeSetMap = edgeTypeSetMap;
    }

    Map<GGNNEdgeType, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>> getEdgesByType() {
        return Map.copyOf(edgeTypeSetMap);
    }

    @Override
    public DotGraph<IdentityWrapper<AstNode>> asDotGraph() {
        final List<DotGraphEdge<IdentityWrapper<AstNode>>> edges = new ArrayList<>();

        for (final var entry : edgeTypeSetMap.entrySet()) {
            final String edgeColour = getEdgeColour(entry.getKey());

            entry.getValue()
                .stream()
                .map(edge -> new DotGraphEdge<>(edge, edgeColour))
                .forEach(edges::add);
        }

        return new DotGraph<>(graphName, edges, Collections.emptyList(), this.labelGenerator);
    }

    private String getEdgeColour(final GGNNEdgeType edgeType) {
        return switch (edgeType) {
            case CHILD -> "black";
            case NEXT_TOKEN -> "gray50";
            case LAST_WRITE -> "crimson";
            case LAST_READ -> "dodgerblue";
            case COMPUTED_FROM -> "orange";
            case GUARDED_BY -> "purple";
            case RETURNS_TO -> "springgreen4";
        };
    }
}
