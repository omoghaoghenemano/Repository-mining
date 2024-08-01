package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.ForStmt;
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
    public Void defaultAction(AstNode node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> result) {
        for (AstNode child : node.children()) {
            child.accept(this, result);
        }
        return null;
    }

    @Override
    public Void visit(IfStmt ifStmt, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> result) {
        // Process the guard expression of the `if` statement
        IdentityWrapper<AstNode> guard = astNodeMap.get(ifStmt.condition());
        if (guard != null) {
            // Iterate over variables in the `if` statement's body
            for (AstNode variable : ifStmt.children()) {
                IdentityWrapper<AstNode> variableWrapper = astNodeMap.get(variable);
                if (variableWrapper != null) {
                    result.add(new Pair<>(variableWrapper, guard));
                }
            }
        }


        return null;
    }

    @Override
    public Void visit(WhileStmt whileStmt, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> result) {
        // Process the guard expression of the `while` statement
        IdentityWrapper<AstNode> guard = astNodeMap.get(whileStmt.condition());
        if (guard != null) {
            // Iterate over variables in the `while` statement's body
            for (AstNode variable : whileStmt.children()) {
                IdentityWrapper<AstNode> variableWrapper = astNodeMap.get(variable);
                if (variableWrapper != null) {
                    result.add(new Pair<>(variableWrapper, guard));
                }
            }
        }

        // Continue visiting child nodes

        return null;
    }

// Implement to be done similar methods for other relevant node types (e.g., `ForStmt`, `DoWhileStmt`, `CatchClause`, `TernaryExpr`, `SwitchStmt`)

    @Override
    public Void visit(ForStmt forStmt, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> result) {
        // Process the guard expression of the `for` statement
        IdentityWrapper<AstNode> guard = astNodeMap.get(forStmt.forControl());
        if (guard != null) {
            // Iterate over variables in the `for` statement's body
            for (AstNode variable : forStmt.children()) {
                IdentityWrapper<AstNode> variableWrapper = astNodeMap.get(variable);
                if (variableWrapper != null) {
                    result.add(new Pair<>(variableWrapper, guard));
                }
            }
        }

        return null;
    }

}

    // TODO: Implement required visitors

