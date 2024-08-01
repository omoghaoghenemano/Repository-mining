package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Visitor for inferring CHILD edges.
 */
public class ChildVisitor implements AstVisitorWithDefaults<Void, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>> {

    private final IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap;

    public ChildVisitor(IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap) {
        this.astNodeMap = astNodeMap;

    }


    @Override
    public Void defaultAction(AstNode node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> childEdges) {
        IdentityWrapper<AstNode> parentWrapper = astNodeMap.get(node);
        for (AstNode child : node.children()) {
            IdentityWrapper<AstNode> childWrapper = astNodeMap.get(child);
            childEdges.add(Pair.of(parentWrapper, childWrapper));
            defaultAction(child, childEdges);
        }
        return null;
    }
}
