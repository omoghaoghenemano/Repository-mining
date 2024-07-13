// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.enum_declaration;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.Body;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.ClassBodyDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.ClassMemberDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.TypeDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.annotation_declaration.AnnotationMemberDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.interface_declaration.InterfaceMemberDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.Type;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public record EnumDeclaration(
    SimpleIdentifier name,
    List<Type> implementsTypes,
    Body<EnumConstant> enumConstants,
    Body<ClassBodyDeclaration> declarations
) implements ClassMemberDeclaration, TypeDeclaration, InterfaceMemberDeclaration, AnnotationMemberDeclaration,
    Visitable {

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public List<AstNode> children() {
        final List<AstNode> children = new ArrayList<>();
        children.add(name());
        children.addAll(implementsTypes());
        children.add(enumConstants());
        children.add(declarations());
        return children;
    }

    @Override
    public Stream<ClassBodyDeclaration> innerDeclarations() {
        return declarations().declarations().stream();
    }
}
