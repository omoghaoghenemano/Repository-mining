// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg;

import com.google.common.collect.ImmutableList;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.VariableDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.ArrayAccessExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.ParExpression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.ScopedExpression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.binary.AssignmentExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.lambda.LambdaExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.pattern.BasePattern;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.unary.UnaryExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.parameter.Parameter;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.ForStmt;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.LocalVariableDeclStmt;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement.CatchClause;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement.Resources;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.PGNode;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Definition visitor to retrieve every Definition occurring in a given code block. In order to match Definitions to the
 * structure of the ControlFlowGraph and the Scope of each, we use a {@code DefUseVisitorState}. Where the currentNode
 * represents the node in the ControlFlowGraph and the scope one the Scope.
 */
public class DefinitionVisitor
    implements AstVisitorWithDefaults<List<Definition>, DefUseVisitorState> {

    private final ControlFlowGraph cfg;

    private final Set<IdentityWrapper<AstNode>> cfgNodes;

    public DefinitionVisitor(final ControlFlowGraph cfg) {
        this.cfg = cfg;
        cfgNodes = cfg.getVertices().stream().map(PGNode::node).map(IdentityWrapper::of).collect(Collectors.toSet());
    }

    @Override
    public List<Definition> defaultAction(AstNode node, DefUseVisitorState arg) {
        return node.children()
            .stream()
            .flatMap(c -> {
                final DefUseVisitorState state = updateStateNode(arg, node);
                return c.accept(this, state).stream();
            })
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
    public List<Definition> visit(ScopedExpression node, DefUseVisitorState arg) {
        // only care about top-level identifiers
        if (node.expr() instanceof SimpleIdentifier id && arg.scope().isEmpty()) {
            final var def = new Definition(id.name(), id, arg.currentNode().orElse(id), Optional.of(node));
            return List.of(def);
        }

        return node.children()
            .stream()
            .flatMap(c -> c.accept(this, arg.withUpdatedScope(node)).stream())
            .toList();
    }

    @Override
    public List<Definition> visit(MethodDeclaration node, DefUseVisitorState arg) {
        final Stream<AstNode> bodyNodes = node.body().stream().flatMap(b -> b.nodes().stream());
        return Stream.concat(Stream.of(node.parameters()), bodyNodes)
            .flatMap(child -> child.accept(this, arg).stream())
            .toList();
    }

    @Override
    public List<Definition> visit(Parameter.ReceiverParameter node, DefUseVisitorState arg) {
        final var paramDef = new Definition(node.thisIdentifier().toString(), node.thisIdentifier(), node);
        return List.of(paramDef);
    }

    @Override
    public List<Definition> visit(Parameter.FormalParameter node, DefUseVisitorState arg) {
        final var paramDef = new Definition(
            node.identifier().identifier().toString(), node.identifier().identifier(), node
        );
        return List.of(paramDef);
    }

    @Override
    public List<Definition> visit(AssignmentExpr node, DefUseVisitorState arg) {
        final IdentifierCollector identifierCollector = new IdentifierCollector(cfg);
        final List<Definition> leftDefs = node.left().accept(identifierCollector, arg);
        final List<Definition> rightDefs = new ArrayList<>(node.right().accept(this, arg));

        // the definition on the left side supersedes the one on the right
        rightDefs.removeIf(rightDef -> leftDefs.stream().anyMatch(leftDef -> isSameVariable(leftDef, rightDef)));

        return ImmutableList.<Definition>builder().addAll(leftDefs).addAll(rightDefs).build();
    }

    private boolean isSameVariable(final Definition a, final Definition b) {
        return a.name().equals(b.name()) && a.scope().equals(b.scope());
    }

    @Override
    public List<Definition> visit(UnaryExpr node, DefUseVisitorState arg) {
        if (hasDefinition(node) && directlyAffectsElement(node)) {
            final IdentifierCollector v = new IdentifierCollector(cfg);
            return node.expression().accept(v, arg);
        }
        else {
            return defaultAction(node, arg);
        }
    }

    private boolean directlyAffectsElement(final UnaryExpr expr) {
        final boolean direct = expr.expression() instanceof SimpleIdentifier;
        final boolean parenthesised = expr.expression() instanceof ParExpression parExpr
            && parExpr.expression() instanceof SimpleIdentifier;
        final boolean scoped = expr.expression() instanceof ScopedExpression scopedExpr
            && scopedExpr.expr() instanceof SimpleIdentifier;
        final boolean arrayAccess = expr.expression() instanceof ArrayAccessExpr;

        return direct || parenthesised || scoped || arrayAccess;
    }

    private boolean hasDefinition(final UnaryExpr expr) {
        return switch (expr.op()) {
            case DECREMENT, INCREMENT -> true;
            default -> false;
        };
    }

    @Override
    public List<Definition> visit(Resources.DeclaredResource node, DefUseVisitorState arg) {
        final var idCollector = new IdentifierCollector(cfg);
        final List<Definition> defs = node.identifier().accept(idCollector, arg);

        final List<Definition> rightDefs = node.init().accept(this, arg);

        return ImmutableList.<Definition>builder().addAll(defs).addAll(rightDefs).build();
    }

    @Override
    public List<Definition> visit(CatchClause node, DefUseVisitorState arg) {
        final List<Definition> allDefs = new ArrayList<>();

        final var exceptionDef = new Definition(node.identifier().toString(), node.identifier(), node);
        allDefs.add(exceptionDef);

        final var blockDefs = node.catchBlock().accept(this, arg);
        allDefs.addAll(blockDefs);

        return allDefs;
    }

    @Override
    public List<Definition> visit(ForStmt.EnhancedFor node, DefUseVisitorState arg) {
        final String itemName = node.variableDeclaratorId().identifier().name();
        final SimpleIdentifier variable = node.variableDeclaratorId().identifier();
        final Definition def = new Definition(itemName, variable, arg.currentNode().orElse(node));

        final List<Definition> otherDefs = new ArrayList<>(node.expression().accept(this, arg));
        otherDefs.add(def);

        return otherDefs;
    }

    @Override
    public List<Definition> visit(
        LocalVariableDeclStmt.LocalVarVariableDecl node, DefUseVisitorState arg
    ) {
        final var def = new Definition(node.identifier().toString(), node.identifier(), arg.currentNode().orElse(node));
        return List.of(def);
    }

    @Override
    public List<Definition> visit(VariableDeclaration node, DefUseVisitorState arg) {
        final var def = new Definition(node.identifier().toString(), node.identifier(), arg.currentNode().orElse(node));
        return List.of(def);
    }

    @Override
    public List<Definition> visit(BasePattern node, DefUseVisitorState arg) {
        final var def = new Definition(node.identifier().toString(), node.identifier(), arg.currentNode().orElse(node));
        return List.of(def);
    }

    @Override
    public List<Definition> visit(LambdaExpr.BlockLambda node, DefUseVisitorState arg) {
        return Collections.emptyList();
    }

    @Override
    public List<Definition> visit(LambdaExpr.ExprLambda node, DefUseVisitorState arg) {
        return Collections.emptyList();
    }

    private static class IdentifierCollector extends DefinitionVisitor {

        IdentifierCollector(ControlFlowGraph cfg) {
            super(cfg);
        }

        @Override
        public List<Definition> visit(SimpleIdentifier node, DefUseVisitorState arg) {
            final var def = new Definition(node.name(), node, arg.currentNode().orElse(node), arg.scope());
            return List.of(def);
        }

        @Override
        public List<Definition> visit(ArrayAccessExpr node, DefUseVisitorState arg) {
            return node.array().accept(this, arg);
        }
    }
}
