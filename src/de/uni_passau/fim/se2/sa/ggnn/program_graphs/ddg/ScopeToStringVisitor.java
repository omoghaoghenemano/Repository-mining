// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.ScopedExpression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;

public final class ScopeToStringVisitor implements AstVisitorWithDefaults<String, Void> {

    @Override
    public String defaultAction(AstNode node, Void arg) {
        return node.children().stream()
            .map(c -> c.accept(this, arg))
            .reduce("", (x, y) -> x + "." + y);
    }

    @Override
    public String visit(ScopedExpression node, Void arg) {
        return node.scope().accept(this, arg);
    }

    @Override
    public String visit(SimpleIdentifier node, Void arg) {
        return node.name();
    }
}
