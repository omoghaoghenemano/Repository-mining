// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The representation of the GGNN graph as input for the ML model.
 *
 * @param label        The graph name.
 * @param labelNodes   The nodes of interest.
 * @param contextGraph The actual graph.
 */
public record GGNNContextGraph(String label, Set<Integer> labelNodes, ContextGraph contextGraph) {

    public record ContextGraph(
        Map<GGNNEdgeType, List<List<Integer>>> edges,
        Map<Integer, String> nodeTypeMap,
        Map<Integer, String> nodeLabelMap
    ) {
    }
}
