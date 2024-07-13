// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.ImportDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.PackageDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.TypeDeclarator;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record CompilationUnit(
    Optional<PackageDeclaration> packageDeclaration,
    List<ImportDeclaration> importDeclarations,
    List<TypeDeclarator> typeDeclarations
) implements AstNode, Visitable {

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public List<AstNode> children() {
        final var children = new ArrayList<AstNode>();
        packageDeclaration().ifPresent(children::add);
        children.addAll(importDeclarations());
        children.addAll(typeDeclarations());
        return children;
    }
}
