// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs.cfg;

import de.uni_passau.fim.se2.sa.ggnn.program_graphs.PGNode;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.ProgramGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;

public class ControlFlowGraph extends ProgramGraph<ControlFlowGraph> {

    public ControlFlowGraph(String graphName, Graph<PGNode, DefaultEdge> graph) {
        super(graphName, graph);
    }

    @Override
    public ControlFlowGraph reversedEntryExitNotSwapped() {
        return new ControlFlowGraph(graphName, new EdgeReversedGraph<>(graph));
    }

    @Override
    public ControlFlowGraph reversed() {
        return new ControlFlowGraph(graphName, reversedGraph());
    }
}
