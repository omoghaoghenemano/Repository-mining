// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg.data_flow_analysis;

import de.uni_passau.fim.se2.sa.ggnn.program_graphs.PGNode;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.ProgramGraph;

import java.util.Set;

public interface FlowDirection<T extends ProgramGraph<T>> {

    Set<PGNode> getInNodes(T graph, PGNode node);

    Set<PGNode> getOutNodes(T graph, PGNode node);

    Set<PGNode> getInitialNodes(T graph);

    final class ForwardFlowDirection<T extends ProgramGraph<T>> implements FlowDirection<T> {

        @Override
        public Set<PGNode> getInNodes(T graph, PGNode node) {
            return graph.getImmediatePredecessors(node);
        }

        @Override
        public Set<PGNode> getOutNodes(T graph, PGNode node) {
            return graph.getImmediateSuccessors(node);
        }

        @Override
        public Set<PGNode> getInitialNodes(T graph) {
            return Set.of(graph.entryNode());
        }
    }

    final class BackwardFlowDirection<T extends ProgramGraph<T>> implements FlowDirection<T> {

        @Override
        public Set<PGNode> getInNodes(T graph, PGNode node) {
            return graph.getImmediateSuccessors(node);
        }

        @Override
        public Set<PGNode> getOutNodes(T graph, PGNode node) {
            return graph.getImmediatePredecessors(node);
        }

        @Override
        public Set<PGNode> getInitialNodes(T graph) {
            return Set.of(graph.exitNode());
        }
    }
}
