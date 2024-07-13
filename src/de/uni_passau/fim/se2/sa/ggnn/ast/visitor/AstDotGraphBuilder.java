// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.visitor;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.annotation_declaration.AnnotationDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.enum_declaration.EnumDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.interface_declaration.InterfaceDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.record_declaration.RecordDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.util.dotgraph.AstNodeLabelGenerator;
import de.uni_passau.fim.se2.sa.ggnn.util.dotgraph.DotGraph;
import de.uni_passau.fim.se2.sa.ggnn.util.dotgraph.DotGraphEdge;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class AstDotGraphBuilder {

    private AstDotGraphBuilder() {
        throw new IllegalCallerException("utility class");
    }

    static DotGraph<AstNode> buildGraph(final TypeDeclarator typeDeclarator) {
        final List<DotGraph<AstNode>> subGraphs = typeDeclarator.declaration().innerDeclarations()
            .map(n -> {
                final String title = getTitleForRootNode(n);
                return buildGraph(n, title);
            })
            .toList();
        final List<DotGraphEdge<AstNode>> graphEdges = getEdges(typeDeclarator, true);

        return new DotGraph<>(
            typeDeclarator.declaration().name().name(), graphEdges, subGraphs, AstNodeLabelGenerator::getLabel
        );
    }

    static DotGraph<AstNode> buildGraph(final MemberDeclarator<MethodDeclaration> method) {
        return buildGraph(method, getMethodName(method.declaration()));
    }

    static DotGraph<AstNode> buildGraph(final AstNode root, final String title) {
        return new DotGraph<>(title, getEdges(root, false), Collections.emptyList(), AstNodeLabelGenerator::getLabel);
    }

    private static List<DotGraphEdge<AstNode>> getEdges(final AstNode root, boolean skipInnerDeclarations) {
        final AstEdgesVisitor visitor = new AstEdgesVisitor(skipInnerDeclarations);
        return root.accept(visitor, null).map(DotGraphEdge::new).toList();
    }

    private static String getTitleForRootNode(final AstNode root) {
        final GraphTitleVisitor titleVisitor = new GraphTitleVisitor();
        return root.accept(titleVisitor, null);
    }

    private static String getMethodName(final MethodDeclaration methodDecl) {
        return methodDecl.name().name() + "(…)";
    }

    private record AstEdgesVisitor(boolean skipInnerDeclarations)
        implements AstVisitorWithDefaults<Stream<Pair<AstNode, AstNode>>, Void> {

        @Override
        public Stream<Pair<AstNode, AstNode>> defaultAction(AstNode node, Void arg) {
            final var childEdges = node.children().stream().map(c -> new Pair<>(node, c));
            final var remainingGraph = node.children().stream().flatMap(c -> c.accept(this, arg));
            return Stream.concat(childEdges, remainingGraph);
        }

        @Override
        public <D extends AstNode> Stream<Pair<AstNode, AstNode>> visit(MemberDeclarator<D> node, Void arg) {
            if (skipInnerDeclarations()) {
                return Stream.empty();
            }
            else {
                return defaultAction(node, arg);
            }
        }
    }

    /**
     * Determines the title of a (sub-)graph depending on the type of the root node.
     */
    private static final class GraphTitleVisitor implements AstVisitorWithDefaults<String, Void> {

        @Override
        public String defaultAction(AstNode node, Void arg) {
            return node.getClass().getSimpleName();
        }

        @Override
        public <D extends AstNode> String visit(MemberDeclarator<D> node, Void arg) {
            return node.declaration().accept(this, arg);
        }

        @Override
        public String visit(AnnotationDeclaration node, Void arg) {
            return node.name().name();
        }

        @Override
        public String visit(EnumDeclaration node, Void arg) {
            return node.name().name();
        }

        @Override
        public String visit(InterfaceDeclaration node, Void arg) {
            return node.name().name();
        }

        @Override
        public String visit(RecordDeclaration node, Void arg) {
            return node.name().name();
        }

        @Override
        public String visit(ClassDeclaration node, Void arg) {
            return node.name().name();
        }

        @Override
        public String visit(FieldDeclaration node, Void arg) {
            return node
                .attributes()
                .stream()
                .map(decl -> decl.identifier().identifier().name())
                .collect(Collectors.joining(", "));
        }

        @Override
        public String visit(MethodDeclaration node, Void arg) {
            return getMethodName(node);
        }
    }
}
