// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg.data_flow_analysis;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.PGNode;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg.DataFlowFact;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg.DataFlowFacts;

import java.util.*;
import java.util.stream.Collectors;

public class DataFlowAnalysis<T extends DataFlowFact> {

    private final TransferFunction<T> transferFunction;
    private final JoinFunction<T> joinFunction;
    private final FlowDirection<ControlFlowGraph> flowDirection;

    private final Class<T> factType;

    public DataFlowAnalysis(
        final TransferFunction<T> transferFunction, final JoinFunction<T> joinFunction,
        final FlowDirection<ControlFlowGraph> flowDirection, final Class<T> factType
    ) {
        this.transferFunction = transferFunction;
        this.joinFunction = joinFunction;
        this.flowDirection = flowDirection;
        this.factType = factType;
    }

    public Facts<T> applyAnalysis(final MethodDeclaration methodDeclaration) {
        final DataFlowFacts dataFlowFacts = new DataFlowFacts(methodDeclaration);
        return applyAnalysis(dataFlowFacts);
    }

    public record Facts<T>(Map<PGNode, Set<T>> inFacts, Map<PGNode, Set<T>> outFacts) {
    }

    private Facts<T> applyAnalysis(final DataFlowFacts facts) {
        final Map<PGNode, Set<T>> dataFlowFacts = initFacts(facts);
        final Map<PGNode, Set<T>> outFacts = new HashMap<>(dataFlowFacts);
        final Deque<PGNode> workList = new ArrayDeque<>(flowDirection.getInitialNodes(facts.getCfg()));

        while (!workList.isEmpty()) {
            final PGNode node = workList.poll();

            final Set<T> lastOut = outFacts.get(node);
            final Set<T> inFacts = inFacts(outFacts, facts.getCfg(), node);
            dataFlowFacts.put(node, inFacts);
            final Set<T> newOut = transferFunction.apply(node, inFacts);

            if (!lastOut.equals(newOut)) {
                outFacts.put(node, newOut);
                workList.addAll(flowDirection.getOutNodes(facts.getCfg(), node));
            }
        }

        return new Facts<>(dataFlowFacts, outFacts);
    }

    private Set<T> inFacts(final Map<PGNode, Set<T>> outFacts, final ControlFlowGraph cfg, final PGNode node) {
        final Set<PGNode> inNodes = flowDirection.getInNodes(cfg, node);
        final Set<Set<T>> facts = inNodes.stream().map(outFacts::get).collect(Collectors.toSet());
        return joinFunction.apply(facts);
    }

    private Map<PGNode, Set<T>> initFacts(final DataFlowFacts dataFlowFacts) {
        final ControlFlowGraph cfg = dataFlowFacts.getCfg();

        final Map<PGNode, Set<T>> facts = new HashMap<>();

        if (joinFunction instanceof JoinFunction.MayFunction<T>) {
            final Set<T> getAll = dataFlowFacts.getFacts(factType);
            cfg.getVertices().forEach(node -> facts.put(node, new HashSet<>(getAll)));
        }
        else {
            assert joinFunction instanceof JoinFunction.MustFunction;
            cfg.getVertices().forEach(node -> facts.put(node, new HashSet<>()));
        }

        return facts;
    }
}
