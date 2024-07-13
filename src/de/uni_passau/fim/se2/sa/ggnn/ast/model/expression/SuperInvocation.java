// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.expression;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.Arguments;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.TypeArgument;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.ArrayList;
import java.util.List;

public interface SuperInvocation extends InvocationExpression {

    record SuperConstructorInvocation(Arguments arguments)
        implements SuperInvocation, Visitable {

        @Override
        public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
            return visitor.visit(this, arg);
        }

        @Override
        public List<AstNode> children() {
            return List.of(arguments());
        }
    }

    record SuperMethodInvocation(
        List<TypeArgument> typeArguments,
        SimpleIdentifier name,
        Arguments arguments
    ) implements SuperInvocation, Visitable {

        @Override
        public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
            return visitor.visit(this, arg);
        }

        @Override
        public List<AstNode> children() {
            final var children = new ArrayList<AstNode>(typeArguments());
            children.add(name());
            children.add(arguments());
            return children;
        }
    }
}
