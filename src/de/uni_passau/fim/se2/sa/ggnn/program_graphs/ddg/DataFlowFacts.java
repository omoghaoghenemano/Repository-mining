// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.parameter.Parameter;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.PGNode;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.cfg.CfgBuilder;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg.data_flow_analysis.*;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class DataFlowFacts {

    private final MethodDeclaration methodDeclaration;
    private final ControlFlowGraph cfg;

    private final Map<PGNode, List<Use>> uses;
    private final Map<PGNode, List<Definition>> definitions;

    private Optional<List<Pair<Definition, Use>>> defUsePairs = Optional.empty();

    private Optional<List<Pair<Use, Use>>> lastReads = Optional.empty();

    public DataFlowFacts(final MethodDeclaration method) {
        methodDeclaration = method;
        cfg = new CfgBuilder().build(method);

        uses = collectUses(method);
        definitions = collectDefinitions(method);
    }

    private Map<PGNode, List<Use>> collectUses(final MethodDeclaration method) {
        final var useVisitor = new UseVisitor(cfg);
        final List<Use> uses = method.accept(useVisitor, DefUseVisitorState.emptyState());
        return uses.stream().collect(Collectors.groupingBy(DataFlowFacts::getCfgNode));
    }

    private Map<PGNode, List<Definition>> collectDefinitions(final MethodDeclaration method) {
        final var defVisitor = new DefinitionVisitor(cfg);
        final List<Definition> defs = method.accept(defVisitor, DefUseVisitorState.emptyState());
        return defs.stream().collect(Collectors.groupingBy(DataFlowFacts::getCfgNode));
    }

    /**
     * Gets all data flow facts of the given type.
     *
     * @param factType The type of facts.
     * @return A set of all known data flow facts of the requested type.
     * @param <T> The type of facts.
     */
    @SuppressWarnings("unchecked")
    public <T extends DataFlowFact> Set<T> getFacts(final Class<T> factType) {
        if (Definition.class.equals(factType)) {
            final var defs = definitions.values().stream().flatMap(List::stream).collect(Collectors.toSet());
            return (Set<T>) new HashSet<>(defs);
        }
        else if (Use.class.equals(factType)) {
            final var use = uses.values().stream().flatMap(List::stream).collect(Collectors.toSet());
            return (Set<T>) new HashSet<>(use);
        }
        else {
            throw new IllegalStateException("Only definitions and uses are known data flow facts.");
        }
    }

    public Map<PGNode, List<Definition>> getDefinitions() {
        return Map.copyOf(definitions);
    }

    public Map<PGNode, List<Use>> getUses() {
        return Map.copyOf(uses);
    }

    public ControlFlowGraph getCfg() {
        return cfg;
    }

    private static PGNode getCfgNode(final DataFlowFact dataFlowFact) {
        final AstNode cfgNode = dataFlowFact.cfgNode();
        if (cfgNode instanceof Parameter.FormalParameter || cfgNode instanceof Parameter.ReceiverParameter) {
            return PGNode.entry();
        }
        else {
            return new PGNode(dataFlowFact.cfgNode());
        }
    }

    /**
     * Gets all def-use pairs, i.e. last writes.
     *
     * @return All use-use pairs.
     */
    public List<Pair<Definition, Use>> getDefUsePairs() {
        if (defUsePairs.isEmpty()) {
            final Map<PGNode, Set<Definition>> reachingDefs = getReachingDefinitions();
            defUsePairs = Optional.of(computeSourceTargetPairs(reachingDefs, uses));
        }
        return defUsePairs.orElseThrow();
    }

    /**
     * Gets all use-use pairs, i.e. last reads.
     *
     * @return All use-use pairs.
     */
    public List<Pair<Use, Use>> getUseUsePairs() {
        if (lastReads.isEmpty()) {
            final Map<PGNode, Set<Use>> reachingUses = getReachingUses();
            lastReads = Optional.of(computeSourceTargetPairs(reachingUses, uses));
        }
        return lastReads.orElseThrow();
    }

    private Map<PGNode, Set<Definition>> getReachingDefinitions() {
        final DataFlowAnalysis<Definition> dataFlowAnalysis = new DataFlowAnalysisBuilder<>(Definition.class)
            .withFlowDirection(new FlowDirection.ForwardFlowDirection<>())
            .withJoin(new JoinFunction.MayFunction<>())
            .withTransfer(new ReachingDefTransferFunction(definitions))
            .build();
        return dataFlowAnalysis.applyAnalysis(methodDeclaration).outFacts();
    }

    private Map<PGNode, Set<Use>> getReachingUses() {
        final DataFlowAnalysis<Use> dataFlowAnalysis = new DataFlowAnalysisBuilder<>(Use.class)
            .withFlowDirection(new FlowDirection.ForwardFlowDirection<>())
            .withJoin(new JoinFunction.MayFunction<>())
            .withTransfer(new ReachingUseTransferFunction(uses))
            .build();
        return dataFlowAnalysis.applyAnalysis(methodDeclaration).inFacts();
    }

    private <A extends DataFlowFact, B extends DataFlowFact> List<Pair<A, B>> computeSourceTargetPairs(
        final Map<PGNode, Set<A>> reachingDataFlowFacts, final Map<PGNode, List<B>> targetFacts
    ) {
        final List<Pair<A, B>> sourceTargetPairs = new ArrayList<>();

        for (final var entry : targetFacts.entrySet()) {
            final PGNode node = entry.getKey();
            final List<B> targets = entry.getValue();
            final Set<A> sources = reachingDataFlowFacts.getOrDefault(node, Collections.emptySet());

            for (final B target : targets) {
                for (final A source : sources) {
                    if (isSame(source, target)) {
                        sourceTargetPairs.add(Pair.of(source, target));
                    }
                }
            }
        }

        return sourceTargetPairs;
    }

    private record ReachingDefTransferFunction(Map<PGNode, List<Definition>> defs)
        implements TransferFunction<Definition> {

        @Override
        public Set<Definition> apply(final PGNode node, final Set<Definition> inFacts) {
            final Set<Definition> result = new HashSet<>(inFacts);

            final List<Definition> nodeDefs = defs.getOrDefault(node, Collections.emptyList());
            nodeDefs.forEach(def -> result.removeIf(r -> isSame(def, r)));
            result.addAll(nodeDefs);

            // inFacts without killed definitions, with new definitions
            return result;
        }
    }

    private record ReachingUseTransferFunction(Map<PGNode, List<Use>> uses) implements TransferFunction<Use> {

        @Override
        public Set<Use> apply(final PGNode node, final Set<Use> inFacts) {
            final Set<Use> result = new HashSet<>(inFacts);

            final List<Use> nodeUses = uses.getOrDefault(node, Collections.emptyList());
            nodeUses.forEach(use -> result.removeIf(r -> r.name().equals(use.name())));
            result.addAll(nodeUses);

            return result;
        }
    }

    private static boolean isSame(final DataFlowFact fact1, final DataFlowFact fact2) {
        return isIdenticalScope(fact1, fact2) && fact1.name().equals(fact2.name());
    }

    private static boolean isIdenticalScope(final DataFlowFact flowFact1, final DataFlowFact flowFact2) {
        if (flowFact1.scope().equals(flowFact2.scope())) {
            return true;
        }

        // If the name of the definition is equal to the scope's name they are identical. Even though the scopes might
        // not be equal. E.g. Object obj = new Object; obj.toString();.
        return flowFact2.scope().stream()
            .map(c -> c.accept(new ScopeToStringVisitor(), null).equals(flowFact1.name()))
            .reduce(false, Boolean::logicalOr);
    }
}
