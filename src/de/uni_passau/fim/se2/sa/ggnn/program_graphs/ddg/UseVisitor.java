// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.VariableDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.ArrayAccessExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.MethodInvocation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.ScopedExpression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.binary.AssignmentExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.binary.BinaryExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement.CatchClause;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement.Resources;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement.TryStmt;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.Switch;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.SwitchCase;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.PGNode;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Use visitor to retrieve every Use occurring in a given code block. In order to match Uses to the structure of the
 * ControlFlowGraph and the Scope of each, we use a {@code DefUseVisitorState}. Where the currentNode represents the
 * node in the ControlFlowGraph and the scope one the Scope.
 */
public class UseVisitor implements AstVisitorWithDefaults<List<Use>, DefUseVisitorState> {

    private final Set<IdentityWrapper<AstNode>> cfgNodes;

    public UseVisitor(final ControlFlowGraph cfg) {
        cfgNodes = cfg.getVertices().stream().map(PGNode::node).map(IdentityWrapper::of).collect(Collectors.toSet());
    }

    @Override
    public List<Use> defaultAction(AstNode node, DefUseVisitorState arg) {
        return node.children().stream()
            .flatMap(c -> {
                final DefUseVisitorState state = updateStateNode(arg, node);
                return c.accept(this, state).stream();
            })
            .filter(u -> u.cfgNode() != null)
            .toList();
    }

    private DefUseVisitorState updateStateNode(final DefUseVisitorState state, final AstNode currentNode) {
        if (cfgNodes.contains(IdentityWrapper.of(currentNode))) {
            return state.withUpdatedNode(currentNode);
        }
        else {
            return state;
        }
    }

    @Override
    public List<Use> visit(MethodDeclaration node, DefUseVisitorState arg) {
        return node
            .body()
            .stream()
            .flatMap(b -> b.nodes().stream())
            .flatMap(child -> child.accept(this, arg).stream())
            .toList();
    }

    @Override
    public List<Use> visit(VariableDeclaration node, DefUseVisitorState arg) {
        return node.initializer().stream().flatMap(c -> c.accept(this, arg).stream()).toList();
    }

    @Override
    public List<Use> visit(SimpleIdentifier node, DefUseVisitorState arg) {
        return List.of(new Use(node.name(), node, arg.currentNode().orElse(node), arg.scope()));
    }

    @Override
    public List<Use> visit(ExpressionStmt node, DefUseVisitorState arg) {
        return node.children().stream()
            .flatMap(c -> c.accept(this, arg.withUpdatedNode(node)).stream())
            .toList();
    }

    @Override
    public List<Use> visit(AssignmentExpr node, DefUseVisitorState arg) {
        final var allUses = new ArrayList<>(node.right().accept(this, arg));
        final var leftUses = node.left().accept(new AssignmentExpressionLeftSideVisitor(), null).stream()
            .flatMap(n -> n.accept(this, arg).stream())
            .toList();
        allUses.addAll(leftUses);
        return allUses;
    }

    @Override
    public List<Use> visit(
        LocalVariableDeclStmt.LocalTypedVariableDecl node, DefUseVisitorState arg
    ) {
        return node.declarations().stream()
            .flatMap(
                decl -> decl.accept(this, arg.currentNode().isPresent() ? arg : arg.withUpdatedNode(node)).stream()
            )
            .toList();
    }

    @Override
    public List<Use> visit(BinaryExpr node, DefUseVisitorState arg) {
        return node.children().stream()
            .flatMap(c -> c.accept(this, arg).stream())
            .toList();
    }

    @Override
    public List<Use> visit(WhileStmt node, DefUseVisitorState arg) {
        final var allUses = new ArrayList<>(
            node.condition().accept(this, arg.withUpdatedNode(node))
        );
        final var childUses = node.block().accept(this, arg).stream()
            .toList();
        allUses.addAll(childUses);
        return allUses;
    }

    @Override
    public List<Use> visit(DoWhileStmt node, DefUseVisitorState arg) {
        final var allUses = new ArrayList<>(
            node.condition().accept(this, arg.withUpdatedNode(node))
        );
        final var childUses = node.block().accept(this, arg).stream()
            .toList();
        allUses.addAll(childUses);
        return allUses;
    }

    @Override
    public List<Use> visit(IfStmt node, DefUseVisitorState arg) {
        final var allUses = new ArrayList<>(
            node.condition().accept(this, arg.withUpdatedNode(node))
        );
        final var thenUses = node.thenStmt().accept(this, arg).stream()
            .toList();
        final var elseUses = node.elseStmt().stream()
            .flatMap(elseStmt -> elseStmt.accept(this, arg).stream())
            .toList();
        allUses.addAll(thenUses);
        allUses.addAll(elseUses);
        return allUses;
    }

    @Override
    public List<Use> visit(ForStmt node, DefUseVisitorState arg) {
        final var allUses = new ArrayList<>(
            node.forControl().accept(this, arg.withUpdatedNode(node))
        );
        final var blockUses = node.statementBlock().accept(this, arg).stream()
            .toList();
        allUses.addAll(blockUses);
        return allUses;
    }

    @Override
    public List<Use> visit(Switch.SwitchStmt node, DefUseVisitorState arg) {
        final var allUses = new ArrayList<>(
            node.check().accept(this, arg.withUpdatedNode(node))
        );
        final var blockUses = node.cases().stream().flatMap(c -> c.accept(this, arg).stream())
            .toList();
        allUses.addAll(blockUses);
        return allUses;
    }

    @Override
    public List<Use> visit(SwitchCase node, DefUseVisitorState arg) {
        final var allUses = new ArrayList<>(node.label().accept(this, arg.withUpdatedNode(node)));
        final var usesInBlock = node.block().accept(this, arg);
        allUses.addAll(usesInBlock);
        return allUses;
    }

    @Override
    public List<Use> visit(ThrowStmt node, DefUseVisitorState arg) {
        return node.children().stream()
            .flatMap(c -> c.accept(this, arg.withUpdatedNode(node)).stream())
            .toList();
    }

    @Override
    public List<Use> visit(ReturnStmt node, DefUseVisitorState arg) {
        return node.children().stream()
            .flatMap(c -> c.accept(this, arg.withUpdatedNode(node)).stream())
            .toList();
    }

    @Override
    public List<Use> visit(AssertStmt node, DefUseVisitorState arg) {
        return node.children().stream()
            .flatMap(c -> c.accept(this, arg.withUpdatedNode(node)).stream())
            .toList();
    }

    @Override
    public List<Use> visit(SynchronizedStmt node, DefUseVisitorState arg) {
        final var allUses = new ArrayList<>(node.monitor().accept(this, arg.withUpdatedNode(node)));
        final var usesInBlock = node.block().accept(this, arg);
        allUses.addAll(usesInBlock);
        return allUses;
    }

    @Override
    public List<Use> visit(TryStmt.TryWithResources node, DefUseVisitorState arg) {
        final var allUses = new ArrayList<>(node.resourceSpecStmt().accept(this, arg.withUpdatedNode(node)));
        final var usesInBlock = node.block().accept(this, arg);
        final var usesInCatch = node.catchClauses().stream()
            .flatMap(c -> c.accept(this, arg).stream())
            .toList();
        final var usesInFinally = node.finallyBlock().stream()
            .flatMap(f -> f.accept(this, arg).stream())
            .toList();
        allUses.addAll(usesInBlock);
        allUses.addAll(usesInCatch);
        allUses.addAll(usesInFinally);
        return allUses;
    }

    @Override
    public List<Use> visit(CatchClause node, DefUseVisitorState arg) {
        final DefUseVisitorState state = updateStateNode(arg, node);
        return node.catchBlock().nodes().stream().flatMap(c -> c.accept(this, state).stream()).toList();
    }

    @Override
    public List<Use> visit(Resources.DeclaredResource node, DefUseVisitorState arg) {
        return node.init().accept(this, arg);
    }

    @Override
    public List<Use> visit(MethodInvocation node, DefUseVisitorState arg) {
        return node.arguments().accept(this, arg.withoutScope());
    }

    @Override
    public List<Use> visit(ForStmt.EnhancedFor node, DefUseVisitorState arg) {
        return node.expression().accept(this, arg);
    }

    @Override
    public List<Use> visit(ForStmt.RegularFor node, DefUseVisitorState arg) {
        final var allUses = node.condition().stream()
            .flatMap(c -> c.accept(this, arg).stream())
            .collect(Collectors.toCollection(ArrayList::new));
        allUses.addAll(node.update().stream().flatMap(c -> c.accept(this, arg).stream()).toList());
        allUses.addAll(node.init().stream().flatMap(c -> c.accept(this, arg).stream()).toList());
        return allUses;
    }

    @Override
    public List<Use> visit(ScopedExpression node, DefUseVisitorState arg) {
        return node.children().stream()
            .flatMap(c -> c.accept(this, arg.withUpdatedScope(node)).stream())
            .toList();
    }

    /**
     * Checks if the left side of an assignment contains a use, e.g. arr[42] = ...
     */
    private static final class AssignmentExpressionLeftSideVisitor
        implements AstVisitorWithDefaults<List<AstNode>, Void> {

        @Override
        public List<AstNode> defaultAction(AstNode node, Void arg) {
            return node.children().stream()
                .flatMap(c -> c.accept(this, arg).stream())
                .filter(Objects::nonNull)
                .toList();
        }

        @Override
        public List<AstNode> visit(ArrayAccessExpr node, Void arg) {
            final var allUses = new ArrayList<>(node.array().accept(this, arg));
            allUses.add(node.index());
            return allUses;
        }
    }
}
