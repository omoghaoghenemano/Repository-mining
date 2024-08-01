package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.util.*;

/**
 * Visitor for inferring NEXT_TOKEN edges.
 */
public class NextTokenVisitor implements
        AstVisitorWithDefaults<Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>> {

    private final IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap;

    private final Queue<AstNode> nodeQueue = new LinkedList<>();
    private final Set<AstNode> visitedNodes = new HashSet<>();

    public NextTokenVisitor(IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap) {
        this.astNodeMap = astNodeMap;
    }

    @Override
    public Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> defaultAction(
            AstNode node,
            Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> nextTokenEdges) {

        // Add the current node to the queue if not already processed
        if (visitedNodes.add(node)) {
            nodeQueue.add(node);
        }

        // Process nodes in the queue
        while (!nodeQueue.isEmpty()) {
            AstNode currentNode = nodeQueue.poll();
            IdentityWrapper<AstNode> currentWrapper = astNodeMap.get(currentNode);

            // Get the successor nodes based on your AST structure
            List<AstNode> successors = getSuccessors(currentNode);

            for (AstNode successor : successors) {
                if (visitedNodes.add(successor)) {
                    IdentityWrapper<AstNode> successorWrapper = astNodeMap.get(successor);
                    nextTokenEdges.add(new Pair<>(currentWrapper, successorWrapper));
                    nodeQueue.add(successor);
                }
            }
        }

        return nextTokenEdges;
    }

    // Method to get the successor nodes
    private List<AstNode> getSuccessors(AstNode node) {
        // Implement logic to get the list of successor nodes for the given node
        // This implementation will depend on your AST structure and how you determine the sequence of nodes

        // Example: Assuming you have a method `getChildren` that returns children nodes
        // and the next token is the next child node in the sequence
        List<AstNode> successors = new ArrayList<>();
        List<AstNode> children = getChildren(node);

        // Example of simple linear sequence where each node has an ordered list of children
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).equals(node) && i + 1 < children.size()) {
                successors.add(children.get(i + 1));
                break;
            }
        }

        return successors;
    }

    // Example method to get children nodes; replace with actual method
    private List<AstNode> getChildren(AstNode node) {
        // This should be implemented based on how your AST structure provides children nodes
        return node.children();  // Assuming `children` method exists
    }

    // TODO: Implement other required visitors
}