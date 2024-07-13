// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs.cfg;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.PGBuilder;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.PGNode;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.ProgramGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;
import java.util.stream.Stream;

public class CfgBuilder extends PGBuilder<ControlFlowGraph> {

    @Override
    public ControlFlowGraph build(final MethodDeclaration method) {
        return buildGraph(method);
    }

    private ControlFlowGraph buildGraph(final MethodDeclaration method) {
        collectEdges(method);

        final Graph<PGNode, DefaultEdge> graph = ProgramGraph.emptyBaseGraph();
        edges.stream()
            .flatMap(edge -> Stream.of(edge.a(), edge.b()))
            .forEach(graph::addVertex);
        edges.forEach(edge -> graph.addEdge(edge.a(), edge.b()));

        final String graphName = method.name().name();

        return new ControlFlowGraph(graphName, graph);
    }

    private void collectEdges(final MethodDeclaration method) {
        final CfgBuildingVisitor visitor = new CfgBuildingVisitor(this);
        final List<PGNode> terminalNodes = method.accept(visitor, List.of(entry));
        terminalNodes.forEach(n -> addEdge(n, exit));

        visitor.getUnresolvedThrows()
            .stream()
            .map(PGNode::new)
            .map(this::getNode)
            .forEach(this::addEdgeToExit);
    }
}
