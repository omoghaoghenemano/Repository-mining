// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.preprocessor.shared;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;

import java.util.stream.Stream;

public record MethodsVisitor(boolean includeAbstractMethods)
    implements AstVisitorWithDefaults<Stream<MethodDeclaration>, Void> {

    @Override
    public Stream<MethodDeclaration> defaultAction(AstNode node, Void arg) {
        return node.children().stream().flatMap(child -> child.accept(this, arg));
    }

    @Override
    public Stream<MethodDeclaration> visit(MethodDeclaration node, Void arg) {
        return Stream.concat(
            Stream.of(node).filter(n -> includeAbstractMethods || node.body().isPresent()),
            node.children().stream().flatMap(c -> c.accept(this, arg))
        );
    }
}
