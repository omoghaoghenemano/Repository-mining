package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.ReturnStmt;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Visitor for inferring RETURNS_TO edges.
 */
public class ReturnsToVisitor implements AstVisitorWithDefaults<Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>, Void> {

    private final IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap;
    private IdentityWrapper<AstNode> methodDecl;

    public ReturnsToVisitor(IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap) {
        this.astNodeMap = astNodeMap;
    }

    @Override
    public Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> defaultAction(AstNode node, Void arg) {
        Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> edges = new HashSet<>();
        if (node instanceof MethodDeclaration) {
            methodDecl = astNodeMap.get(node);
            for (AstNode child : node.children()) {
                edges.addAll(child.accept(this, arg));
            }
            methodDecl = null; // Reset after visiting the method
        } else if (node instanceof ReturnStmt) {
            IdentityWrapper<AstNode> returnNode = astNodeMap.get(node);
            if (methodDecl != null && returnNode != null) {
                edges.add(new Pair<>(returnNode, methodDecl));
            }
        } else {
            for (AstNode child : node.children()) {
                edges.addAll(child.accept(this, arg));
            }
        }
        return edges;
    }

    // Implement visit methods for specific node types if necessary
}
    // TODO: Implement required visitors

