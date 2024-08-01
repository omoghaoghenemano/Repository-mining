package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.binary.AssignmentExpr;
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
        // Default action to handle other nodes
        for (AstNode child : node.children()) {
            child.accept(this, data);
        }
        return data;
    }

    @Override
    public Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> visit(AssignmentExpr node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> data) {
        Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> computedFrom = new HashSet<>();

        IdentityWrapper<AstNode> leftNode = astNodeMap.get(node.left());
        IdentityWrapper<AstNode> rightNode = astNodeMap.get(node.right());

        if (leftNode != null && rightNode != null) {
            computedFrom.add(new Pair<>(rightNode, leftNode)); // right side is the source, left side is the result
        }

        // Visit children nodes to ensure full traversal
        node.left().accept(this, data);
        node.right().accept(this, data);
        data.addAll(computedFrom);
        return data;
    }

    // Implement other visit methods for different node types if needed
}
    // TODO: Implement required visitors

