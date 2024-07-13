// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.util.dotgraph.AstNodeLabelGenerator;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.util.*;

public final class GGNNContextGraphBuilder {

    private GGNNContextGraphBuilder() {
        throw new IllegalCallerException("utility class constructor");
    }

    public static GGNNContextGraph build(final GGNNGraph graph) {
        final var contextGraph = buildContextGraph(graph);
        return new GGNNContextGraph(graph.name(), contextGraph.b(), contextGraph.a());
    }

    private static Pair<GGNNContextGraph.ContextGraph, Set<Integer>> buildContextGraph(final GGNNGraph ggnnGraph) {
        final Map<Integer, String> nodeLabelMap = new HashMap<>();
        final Map<Integer, String> nodeTypeMap = new HashMap<>();
        final Map<IdentityWrapper<AstNode>, Integer> vertexToIdMap = new HashMap<>();
        final Set<Integer> labelNodes = new HashSet<>();

        int idCounter = 0;
        for (final var vertex : ggnnGraph.graph().vertexSet()) {
            nodeLabelMap.put(idCounter, AstNodeLabelGenerator.getLabel(vertex.elem()));
            nodeTypeMap.put(idCounter, vertex.elem().getClass().getSimpleName());
            vertexToIdMap.put(vertex, idCounter);

            if (ggnnGraph.labelNodes().contains(vertex)) {
                labelNodes.add(idCounter);
            }

            idCounter++;
        }

        final Map<GGNNEdgeType, List<List<Integer>>> idEdgeTypeSetMap = new EnumMap<>(GGNNEdgeType.class);
        for (final var entry : ggnnGraph.getEdgesByType().entrySet()) {
            final var type = entry.getKey();
            final var idEdges = entry.getValue().stream()
                .map(p -> p.map2(vertexToIdMap::get, vertexToIdMap::get))
                .map(p -> List.of(p.a(), p.b()))
                .toList();
            idEdgeTypeSetMap.put(type, idEdges);
        }

        return Pair.of(
            new GGNNContextGraph.ContextGraph(idEdgeTypeSetMap, nodeTypeMap, nodeLabelMap),
            labelNodes
        );
    }
}
