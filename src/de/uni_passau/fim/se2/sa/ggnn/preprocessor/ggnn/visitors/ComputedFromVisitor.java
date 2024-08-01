package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.Expression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.binary.AssignmentExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.binary.BinaryExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Visitor for inferring COMPUTED_FROM edges.
 */
public class ComputedFromVisitor implements
        AstVisitorWithDefaults<Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>> {

    private final IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap;

    public ComputedFromVisitor(IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap) {
        this.astNodeMap = astNodeMap;
    }
    @Override
    public Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> defaultAction(AstNode node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> data) {
        for (AstNode child : node.children()) {
            child.accept(this, data);
        }
        return data;
    }

    @Override
    public Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> visit(AssignmentExpr node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> data) {
        // Visit the right-hand side expression
        node.right().accept(this, data);

        // Handle the assignment
        IdentityWrapper<AstNode> leftNode = astNodeMap.get(node.left());
        collectComputedFromEdges(node.right(), leftNode, data);

        // Visit the left-hand side expression
        node.left().accept(this, data);

        return data;
    }

    private void collectComputedFromEdges(Expression expr, IdentityWrapper<AstNode> target, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> data) {
        if (target == null) {
            return;
        }

        // If the expression is binary, visit its left and right children
        if (expr instanceof BinaryExpr binaryExpr) {
            IdentityWrapper<AstNode> leftChild = astNodeMap.get(binaryExpr.left());
            IdentityWrapper<AstNode> rightChild = astNodeMap.get(binaryExpr.right());

            if (leftChild != null) {
                data.add(new Pair<>(leftChild, target));
            }
            if (rightChild != null) {
                data.add(new Pair<>(rightChild, target));
            }

            // Recursively collect computed from edges for nested binary expressions
            collectComputedFromEdges(binaryExpr.left(), target, data);
            collectComputedFromEdges(binaryExpr.right(), target, data);
        } else {
            // For non-binary expressions, simply add the direct relationship
            IdentityWrapper<AstNode> exprNode = astNodeMap.get(expr);
            if (exprNode != null) {
                data.add(new Pair<>(exprNode, target));
            }
        }
    }

    // Implement other visit methods for different node types if needed
}

    // Implement other visit methods for different node types if needed

    // TODO: Implement required visitors

