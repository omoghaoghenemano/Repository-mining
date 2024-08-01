package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.IfStmt;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.WhileStmt;
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
    public Void defaultAction(AstNode node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> data) {
        // Visit all children nodes
        for (AstNode child : node.children()) {
            child.accept(this, data);
        }
        return null;
    }

    @Override
    public Void visit(IfStmt node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> data) {
        IdentityWrapper<AstNode> conditionNode = astNodeMap.get(node.condition());

        if (conditionNode != null) {
            for (AstNode thenStmt : node.thenStmt().members()) {
                addGuardedByEdges(conditionNode, thenStmt, data);
            }
            if (node.elseStmt() != null) {
                for (AstNode elseStmt : node.elseStmt().get().members()) {
                    addGuardedByEdges(conditionNode, elseStmt, data);
                }
            }
        }

        // Visit children nodes
        node.condition().accept(this, data);
        node.thenStmt().accept(this, data);
        if (node.elseStmt() != null) {
            node.elseStmt().get().accept(this, data);
        }

        return null;
        
    }

    @Override
    public Void visit(WhileStmt node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> data) {
        IdentityWrapper<AstNode> conditionNode = astNodeMap.get(node.condition());

        if (conditionNode != null) {
            for (AstNode bodyStmt : node.block().members()) {
                addGuardedByEdges(conditionNode, bodyStmt, data);
            }
        }

        // Visit children nodes
        node.condition().accept(this, data);
        node.block().accept(this, data);

        return null;
    }

    private void addGuardedByEdges(IdentityWrapper<AstNode> conditionNode, AstNode guardedNode, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> data) {
        IdentityWrapper<AstNode> guardedWrapper = astNodeMap.get(guardedNode);
        if (guardedWrapper != null) {
            data.add(new Pair<>(conditionNode, guardedWrapper));
            // Visit the guarded node to continue traversal
            guardedNode.accept(this, data);
        }
    }
}

    // TODO: Implement required visitors

