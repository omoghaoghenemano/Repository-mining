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
        if (!visitedNodes.contains(node)) {
            nodeQueue.add(node);
            visitedNodes.add(node);
        }

        // Process nodes in the queue
        while (!nodeQueue.isEmpty()) {
            AstNode currentNode = nodeQueue.poll();
            IdentityWrapper<AstNode> currentWrapper = astNodeMap.get(currentNode);

            // Assume nodes are in a sequence and the next node is determined from the current node's children or siblings
            List<AstNode> successors = getSuccessors(currentNode); // Implement this method based on your AST structure

            for (AstNode successor : successors) {
                if (!visitedNodes.contains(successor)) {
                    IdentityWrapper<AstNode> successorWrapper = astNodeMap.get(successor);
                    nextTokenEdges.add(new Pair<>(currentWrapper, successorWrapper));
                    nodeQueue.add(successor);
                    visitedNodes.add(successor);
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
        List<AstNode> successors = new LinkedList<>();
        List<AstNode> children = getChildren(node); // Implement this based on your AST structure

        // Example of simple linear sequence where each node has an ordered list of children
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).equals(node)) {
                if (i + 1 < children.size()) {
                    successors.add(children.get(i + 1));
                }
                break;
            }
        }

        return successors;
    }

    // Example method to get children nodes; replace with actual method
    private List<AstNode> getChildren(AstNode node) {

        return node.children();
    }


        // TODO: Implement required visitors
}
