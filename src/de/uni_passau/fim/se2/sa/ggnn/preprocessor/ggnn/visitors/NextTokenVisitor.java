package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;

/**
 * Visitor for inferring NEXT_TOKEN edges.
 */
public class NextTokenVisitor implements
        AstVisitorWithDefaults<Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>> {

    private final IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap;

    public NextTokenVisitor(IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap) {
        this.astNodeMap = astNodeMap;
    }

    // TODO: Implement required visitors


    @Override
    public Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> defaultAction(AstNode node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> arg) {

        if (arg == null) {
            arg = new HashSet<>();
        }

        List<AstNode> children = node.children();
        for (int i = 0; i < children.size() - 1; i++) {
            AstNode current = children.get(i);
            AstNode next = children.get(i + 1);
            arg.add(Pair.of(astNodeMap.get(current), astNodeMap.get(next)));
        }
        visitChildren(node, arg);
        return arg;
    }

    @Override
    public Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> visitChildren(AstNode node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> arg, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> identity, BinaryOperator<Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>> accumulator) {
        if (arg == null) {
            arg = new HashSet<>();
        }
        for (AstNode child : node.children()) {
            child.accept(this, arg);
        }
        return arg;
    }
}