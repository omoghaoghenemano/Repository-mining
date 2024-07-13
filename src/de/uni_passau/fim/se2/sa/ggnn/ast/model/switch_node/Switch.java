// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.Expression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.Statement;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.ArrayList;
import java.util.List;

public interface Switch extends AstNode {

    Expression check();

    List<SwitchCase> cases();

    record SwitchExpr(Expression check, List<SwitchCase> cases) implements Switch, Expression, Visitable {

        @Override
        public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
            return visitor.visit(this, arg);
        }
    }

    record SwitchStmt(Expression check, List<SwitchCase> cases) implements Switch, Statement, Visitable {

        @Override
        public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
            return visitor.visit(this, arg);
        }
    }

    @Override
    default List<AstNode> children() {
        final var children = new ArrayList<AstNode>();
        children.add(check());
        children.addAll(cases());
        return children;
    }
}
