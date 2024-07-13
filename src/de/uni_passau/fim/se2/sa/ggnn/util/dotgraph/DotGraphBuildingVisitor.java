// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.util.dotgraph;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.ClassDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.TypeDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.TypeDeclarator;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.annotation_declaration.AnnotationDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.enum_declaration.EnumDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.interface_declaration.InterfaceDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.record_declaration.RecordDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.Builder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public record DotGraphBuildingVisitor<N>(Builder<MethodDeclaration, ? extends DotGraphable<N>> builder)
    implements AstVisitorWithDefaults<Stream<DotGraph<N>>, Void> {

    @Override
    public Stream<DotGraph<N>> defaultAction(AstNode node, Void arg) {
        return node.children().stream().flatMap(c -> c.accept(this, arg));
    }

    @Override
    public Stream<DotGraph<N>> visit(MethodDeclaration node, Void arg) {
        return Stream.of(builder.build(node).asDotGraph());
    }

    @Override
    public Stream<DotGraph<N>> visit(TypeDeclarator node, Void arg) {
        return node.declaration().accept(this, arg);
    }

    @Override
    public Stream<DotGraph<N>> visit(AnnotationDeclaration node, Void arg) {
        return visitTypeDeclaration(node, arg);
    }

    @Override
    public Stream<DotGraph<N>> visit(EnumDeclaration node, Void arg) {
        return visitTypeDeclaration(node, arg);
    }

    @Override
    public Stream<DotGraph<N>> visit(InterfaceDeclaration node, Void arg) {
        return visitTypeDeclaration(node, arg);
    }

    @Override
    public Stream<DotGraph<N>> visit(RecordDeclaration node, Void arg) {
        return visitTypeDeclaration(node, arg);
    }

    @Override
    public Stream<DotGraph<N>> visit(ClassDeclaration node, Void arg) {
        return visitTypeDeclaration(node, arg);
    }

    private Stream<DotGraph<N>> visitTypeDeclaration(TypeDeclaration node, Void arg) {
        final List<DotGraph<N>> subGraphs = defaultAction(node, arg).toList();
        final String graphName = AstNodeLabelGenerator.getLabel(node.name());
        return Stream.of(new DotGraph<>(graphName, Collections.emptyList(), subGraphs));
    }
}
