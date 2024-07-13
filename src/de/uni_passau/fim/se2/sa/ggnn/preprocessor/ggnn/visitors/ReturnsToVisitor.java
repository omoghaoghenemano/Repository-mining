package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.util.IdentityHashMap;
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

    // TODO: Implement required visitors
}
