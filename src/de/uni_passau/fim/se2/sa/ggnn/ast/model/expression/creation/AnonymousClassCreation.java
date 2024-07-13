// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.creation;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.Arguments;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.Body;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.ClassBodyDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.Type;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.TypeArgument;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.ArrayList;
import java.util.List;

public record AnonymousClassCreation(
    List<TypeArgument> typeArguments,
    Type className,
    List<TypeArgument> classTypeArguments,
    Arguments arguments,
    Body<ClassBodyDeclaration> body
) implements ClassCreationExpression, Visitable {

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public AnonymousClassCreation withTypeArguments(List<TypeArgument> typeArguments) {
        return new AnonymousClassCreation(typeArguments, className, classTypeArguments, arguments, body);
    }

    @Override
    public List<AstNode> children() {
        final var children = new ArrayList<AstNode>(typeArguments());
        children.add(className());
        children.addAll(classTypeArguments());
        children.add(arguments());
        children.add(body());
        return children;
    }
}
