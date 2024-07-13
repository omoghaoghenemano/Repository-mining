// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg;

import de.uni_passau.fim.se2.sa.ggnn.program_graphs.PGNode;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.ProgramGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;

public class DataDependenceGraph extends ProgramGraph<DataDependenceGraph> {

    public DataDependenceGraph(String graphName, Graph<PGNode, DefaultEdge> graph) {
        super(graphName, graph);
    }

    @Override
    public DataDependenceGraph reversedEntryExitNotSwapped() {
        return new DataDependenceGraph(graphName, new EdgeReversedGraph<>(graph));
    }

    @Override
    public DataDependenceGraph reversed() {
        return new DataDependenceGraph(graphName, reversedGraph());
    }
}
