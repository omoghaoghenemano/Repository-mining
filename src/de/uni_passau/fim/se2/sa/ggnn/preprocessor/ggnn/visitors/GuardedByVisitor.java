package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.TernaryExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.DoWhileStmt;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.ForStmt;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.IfStmt;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.WhileStmt;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement.CatchClause;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.Switch;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Visitor for inferring GUARDED_BY edges.
 */
public class GuardedByVisitor implements AstVisitorWithDefaults<Void, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>> {

    private final IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap;

    public GuardedByVisitor(IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap) {
        this.astNodeMap = astNodeMap;
    }

    @Override
    public Void visit(ForStmt node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> arg) {
        if (node.forControl() instanceof ForStmt.RegularFor regularFor) {
            regularFor.condition().ifPresent(guard -> addGuardedByPair(node, guard, arg));
        } else if (node.forControl() instanceof ForStmt.EnhancedFor enhancedFor) {
            addGuardedByPair(node, enhancedFor.expression(), arg);
        }
        node.children().forEach(child -> child.accept(this, arg));
        return null;
    }

    @Override
    public Void visit(IfStmt node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> arg) {
        // Extract the guard expression for the IfStmt
        AstNode guard = node.condition();
        addGuardedByPair(node, guard, arg);
        // Visit children
        node.children().forEach(child -> child.accept(this, arg));
        return null;
    }

    @Override
    public Void visit(WhileStmt node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> arg) {
        // Extract the guard expression for the WhileStmt
        AstNode guard = node.condition();
        addGuardedByPair(node, guard, arg);
        // Visit children
        node.children().forEach(child -> child.accept(this, arg));
        return null;
    }


    @Override
    public Void visit(DoWhileStmt node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> arg) {
        // Extract the guard expression for the WhileStmt
        AstNode guard = node.condition();
        addGuardedByPair(node, guard, arg);
        // Visit children
        node.children().forEach(child -> child.accept(this, arg));
        return null;
    }


    @Override
    public Void visit(Switch.SwitchStmt node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> arg) {
        addGuardedByPair(node, node.check(), arg);
        node.children().forEach(child -> child.accept(this, arg));
        return null;
    }

    @Override
    public Void visit(CatchClause node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> arg) {
        // In CatchClause, the catchType can be considered as the guard
        addGuardedByPair(node, node.catchType(), arg);
        node.children().forEach(child -> child.accept(this, arg));
        return null;
    }


    private void addGuardedByPair(AstNode node, AstNode guard, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> arg) {
        if (guard != null) {
            IdentityWrapper<AstNode> wrappedNode = astNodeMap.get(node);
            IdentityWrapper<AstNode> wrappedGuard = astNodeMap.get(guard);
            if (wrappedNode != null && wrappedGuard != null) {
                Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>> pair = new Pair<>(wrappedNode, wrappedGuard);
                if (!arg.contains(pair)) {
                    arg.add(pair);
                }
            }
        }
    }

    @Override
    public Void defaultAction(AstNode node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> arg) {
        node.children().forEach(child -> child.accept(this, arg));
        return null;
    }
}





    // TODO: Implement required visitors

