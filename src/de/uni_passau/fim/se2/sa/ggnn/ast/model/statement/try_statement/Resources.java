// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNodeList;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.VariableModifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.Expression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.Statement;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.Type;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.ArrayList;
import java.util.List;

public record Resources(List<Resource> resources) implements AstNodeList<Resources.Resource>, TryComponent, Visitable {

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public List<Resource> nodes() {
        return List.copyOf(resources());
    }

    public interface Resource extends Statement {
    }

    public record DeclaredResource(List<VariableModifier> modifiers, Type type, Identifier identifier, Expression init)
        implements Resource, Visitable {

        @Override
        public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
            return visitor.visit(this, arg);
        }

        @Override
        public List<AstNode> children() {
            final var children = new ArrayList<AstNode>(modifiers());
            children.addAll(List.of(type(), identifier(), init()));
            return children;
        }
    }

    public record ExpressionResource(Expression expression) implements Resource, Visitable {

        @Override
        public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
            return visitor.visit(this, arg);
        }

        @Override
        public List<AstNode> children() {
            return List.of(expression());
        }
    }
}
