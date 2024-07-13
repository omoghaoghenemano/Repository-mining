// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.annotation_declaration;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.interface_declaration.InterfaceMemberDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.List;
import java.util.stream.Stream;

public record AnnotationDeclaration(
    SimpleIdentifier name,
    Body<MemberDeclarator<AnnotationMemberDeclaration>> body
) implements ClassMemberDeclaration, TypeDeclaration, InterfaceMemberDeclaration, AnnotationMemberDeclaration,
    Visitable {

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public List<AstNode> children() {
        return List.of(name(), body());
    }

    @Override
    public Stream<ClassBodyDeclaration> innerDeclarations() {
        return body().declarations().stream().map(ClassBodyDeclaration.class::cast);
    }
}
