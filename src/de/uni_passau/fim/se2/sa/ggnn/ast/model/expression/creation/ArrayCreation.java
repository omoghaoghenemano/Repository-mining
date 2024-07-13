// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.creation;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.Expression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.Type;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.TypeArgument;
import de.uni_passau.fim.se2.sa.ggnn.ast.parser.InternalParseException;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ArrayCreation(
    List<TypeArgument> typeArguments,
    Type arrayType,
    int dimension,
    List<Expression> knownSizes,
    Optional<Expression> initializer
) implements ClassCreationExpression, Visitable {

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public ArrayCreation withTypeArguments(List<TypeArgument> typeArguments) {
        throw new InternalParseException("Java does not support generic arrays.");
    }

    @Override
    public List<AstNode> children() {
        final var children = new ArrayList<AstNode>(typeArguments());
        children.add(arrayType());
        children.addAll(knownSizes());
        initializer().ifPresent(children::add);
        return children;
    }
}
