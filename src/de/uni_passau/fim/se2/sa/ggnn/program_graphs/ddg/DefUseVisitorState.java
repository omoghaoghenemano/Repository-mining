// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;

import java.util.Optional;

public record DefUseVisitorState(Optional<AstNode> currentNode, Optional<AstNode> scope) {

    DefUseVisitorState(final AstNode currentNode, final Optional<AstNode> scope) {
        this(Optional.of(currentNode), scope);
    }

    DefUseVisitorState withUpdatedNode(final AstNode currentNode) {
        return new DefUseVisitorState(currentNode, scope);
    }

    DefUseVisitorState withUpdatedScope(final AstNode scope) {
        return new DefUseVisitorState(currentNode, Optional.of(scope));
    }

    DefUseVisitorState withoutScope() {
        return new DefUseVisitorState(currentNode, Optional.empty());
    }

    public static DefUseVisitorState emptyState() {
        return new DefUseVisitorState(Optional.empty(), Optional.empty());
    }
}
