package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.TernaryExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement.CatchClause;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.Switch;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.util.HashSet;
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
    public Void visit(IfStmt node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> arg) {
        // The condition is a guard for both the then and else blocks

        node.thenStmt().accept(this, arg);
        node.elseStmt().ifPresent(elseStmt -> {
            addGuardedByPair(elseStmt, node.condition(), arg); // Ensure condition guards the else block
            elseStmt.accept(this, arg);
        });
        return null;
    }




    @Override
    public Void visit(WhileStmt node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> arg) {
        // Extract the guard expression for the WhileStmt
        AstNode guard = node.condition();
        addGuardedByPair(node, guard, arg);
        // Visit children

        visitChildren(node, arg);
        return null;
    }


    @Override
    public Void visit(DoWhileStmt node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> arg) {
        // Extract the guard expression for the WhileStmt
        AstNode guard = node.condition();
        addGuardedByPair(node, guard, arg);
        // Visit children
        visitChildren(node, arg);
        return null;
    }


    @Override
    public Void visit(Switch.SwitchStmt node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> arg) {
        addGuardedByPair(node, node.check(), arg);
        visitChildren(node, arg);
        return null;
    }

    @Override
    public Void visit(CatchClause node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> arg) {
        // In CatchClause, the catchType can be considered as the guard
        addGuardedByPair(node, node.catchType(), arg);
        visitChildren(node, arg);
        return null;
    }

    @Override
    public Void visit(TernaryExpr node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> arg) {
        addGuardedByPair(node.thenExpr(), node.elseExpr(), arg);
        visitChildren(node, arg);
        return null;
    }


    private void addGuardedByPair(AstNode node, AstNode guard, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> arg) {
        if (guard != null) {
            Set<SimpleIdentifier> identifiers = new HashSet<>();
            guard.accept(new VariableTokenVisitor(), identifiers);
            IdentityWrapper<AstNode> wrappedNode = astNodeMap.get(node);
            if (wrappedNode != null) {
                for (SimpleIdentifier identifier : identifiers) {
                    IdentityWrapper<AstNode> wrappedGuard = astNodeMap.get(identifier);
                    if (!wrappedGuard.equals(wrappedNode)) {
                        arg.add(new Pair<>(wrappedNode, wrappedGuard));
                    }
                }
            }
        }
    }

    @Override
    public Void defaultAction(AstNode node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> arg) {
        visitChildren(node, arg);
        return null;
    }
}





// TODO: Implement required visitors

