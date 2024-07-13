// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.enum_declaration;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.Arguments;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.annotation.Annotation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.Body;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.ClassBodyDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record EnumConstant(
    List<Annotation> annotations,
    SimpleIdentifier name,
    Optional<Arguments> arguments,
    Optional<Body<ClassBodyDeclaration>> classBody
) implements AstNode, Visitable {

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public List<AstNode> children() {
        final var children = new ArrayList<AstNode>(annotations());
        children.add(name());
        arguments().ifPresent(children::add);
        classBody().ifPresent(children::add);
        return children;
    }
}
