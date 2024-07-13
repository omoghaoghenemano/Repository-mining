// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.GGNNEdgeType;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg.DataFlowFacts;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.util.*;

/**
 * Visitor for building the GGNN-Edges.
 */
public class GGNNEdgesVisitor {

    /**
     * Maps AstNodes to their IdentityWrapper wrapped representation.
     */
    private final IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap = new IdentityHashMap<>();

    /**
     * DataFlowFacts hosting information about the definition and uses of variables.
     */
    private final DataFlowFacts dataFlowFacts;

    private final MethodDeclaration method;

    public GGNNEdgesVisitor(final MethodDeclaration method) {
        this.method = method;
        buildIdentityMap(method);

        dataFlowFacts = new DataFlowFacts(method);
    }

    /**
     * Maps each AstNode to its respective IdentityWrapper representation.
     *
     * @param node the AstNode to be mapped to its IdentityWrapper representation.
     */
    private void buildIdentityMap(AstNode node) {
        astNodeMap.putIfAbsent(node, IdentityWrapper.of(node));
        node.children().forEach(this::buildIdentityMap);
    }

    /**
     * Derives the GGNN edge types from the instantiated method.
     *
     * @return mapping of edge types to the respective set of edges (represented as node pairs).
     */
    public Map<GGNNEdgeType, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>> getEdges() {
        // TODO Implement me
        throw new UnsupportedOperationException("Implement me");
    }

    /**
     * Registers the {@link ChildVisitor} to obtain the set of child edges.
     *
     * @param node based on which the listener will be registered.
     * @return The set of CHILD edges represented as node pairs.
     */
    private Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> getChildEdges(AstNode node) {
        // TODO Implement me
        throw new UnsupportedOperationException("Implement me");
    }

    /**
     * Registers the {@link NextTokenVisitor} to obtain the set of child edges.
     *
     * @param node based on which the listener will be registered.
     * @return The set of NEXT_TOKEN edges represented as node pairs.
     */
    private Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> getNextTokenEdges(AstNode node) {
        // TODO Implement me
        throw new UnsupportedOperationException("Implement me");
    }

    /**
     * Registers the {@link LastWriteVisitor} to obtain the set of child edges.
     *
     * @param node based on which the listener will be registered.
     * @return The set of LAST_WRITE edges represented as node pairs.
     */
    private Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> getLastWriteEdges(AstNode node) {
        // TODO Implement me
        throw new UnsupportedOperationException("Implement me");
    }

    /**
     * Registers the {@link LastReadVisitor} to obtain the set of child edges.
     *
     * @param node based on which the listener will be registered.
     * @return The set of LAST_READ edges represented as node pairs.
     */
    private Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> getLastReadEdges(AstNode node) {
        // TODO Implement me
        throw new UnsupportedOperationException("Implement me");
    }

    /**
     * Registers the {@link ReturnsToVisitor} to obtain the set of child edges.
     *
     * @param node based on which the listener will be registered.
     * @return The set of RETURNS_TO edges represented as node pairs.
     */
    private Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> getReturnsToEdges(AstNode node) {
        // TODO Implement me
        throw new UnsupportedOperationException("Implement me");
    }

    /**
     * Registers the {@link ComputedFromVisitor} to obtain the set of child edges.
     *
     * @param node based on which the listener will be registered.
     * @return The set of COMPUTED_FROM edges represented as node pairs.
     */
    private Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> getComputedFromEdges(AstNode node) {
        // TODO Implement me
        throw new UnsupportedOperationException("Implement me");
    }

    /**
     * Registers the {@link GuardedByVisitor} to obtain the set of child edges.
     *
     * @param node based on which the listener will be registered.
     * @return The set of GUARDED_BY edges represented as node pairs.
     */
    private Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> getGuardedByEdges(AstNode node) {
        // TODO Implement me
        throw new UnsupportedOperationException("Implement me");
    }


}
