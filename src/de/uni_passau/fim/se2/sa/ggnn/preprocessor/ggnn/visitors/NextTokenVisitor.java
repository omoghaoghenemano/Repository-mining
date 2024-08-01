package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Visitor for inferring NEXT_TOKEN edges.
 */
public class NextTokenVisitor implements
        AstVisitorWithDefaults<Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>> {

    private final IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap;
    private final Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> nextTokenEdges = new HashSet<>();

    private AstNode lastVisitedNode = null;

    public NextTokenVisitor(IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap) {
        this.astNodeMap = astNodeMap;
    }
    @Override
    public Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> defaultAction(AstNode node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> param) {
        // Visit all children
        ChildVisitor childVisitor = new ChildVisitor(astNodeMap);
        for (AstNode child : node.children()) {
            child.accept(childVisitor, param);
        }

        // Link the last visited node to the current node
        if (lastVisitedNode != null) {
            nextTokenEdges.add(new Pair<>(
                    astNodeMap.get(lastVisitedNode),
                    astNodeMap.get(node)
            ));
        }

        lastVisitedNode = node;

        return nextTokenEdges;
    }

    public IdentityHashMap<AstNode, IdentityWrapper<AstNode>> getAstNodeMap() {
        return astNodeMap;
    }

    public Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> getNextTokenEdges() {
        return nextTokenEdges;
    }


}

