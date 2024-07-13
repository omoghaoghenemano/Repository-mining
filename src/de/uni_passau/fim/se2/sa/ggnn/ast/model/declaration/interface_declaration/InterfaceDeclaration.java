// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.interface_declaration;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.annotation_declaration.AnnotationMemberDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.Type;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.TypeParameter;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public record InterfaceDeclaration(
    SimpleIdentifier name,
    List<TypeParameter> typeParameters,
    List<Type> extendsTypes,
    List<Type> permitsTypes,
    Body<MemberDeclarator<InterfaceMemberDeclaration>> body
) implements ClassMemberDeclaration, TypeDeclaration, InterfaceMemberDeclaration, AnnotationMemberDeclaration,
    Visitable {

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public List<AstNode> children() {
        final var children = new ArrayList<AstNode>();
        children.add(name());
        children.addAll(typeParameters());
        children.addAll(extendsTypes());
        children.addAll(permitsTypes());
        children.add(body());
        return children;
    }

    @Override
    public Stream<ClassBodyDeclaration> innerDeclarations() {
        return body().declarations().stream().map(ClassBodyDeclaration.class::cast);
    }
}
