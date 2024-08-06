package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn;


import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.util.dotgraph.AstNodeLabelGenerator;
import de.uni_passau.fim.se2.sa.ggnn.util.dotgraph.DotGraph;
import de.uni_passau.fim.se2.sa.ggnn.util.dotgraph.DotGraphEdge;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GGNNGraphTest {

    private GGNNGraph graph;
    public IdentityWrapper<AstNode> nodeA;
    private IdentityWrapper<AstNode> nodeB;
    private IdentityWrapper<AstNode> nodeC;

    @BeforeEach
    public void setUp() {
        // Create sample nodes using anonymous inner classes
        nodeA = IdentityWrapper.of(new AstNode() {
            @Override
            public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
                return null;
            }

            @Override
            public List<AstNode> children() {
                return List.of();
            }

            @Override
            public String toString() {
                return "NodeA";
            }
        });
        nodeB = IdentityWrapper.of(new AstNode() {
            @Override
            public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
                return null;
            }

            @Override
            public List<AstNode> children() {
                return List.of();
            }

            @Override
            public String toString() {
                return "NodeB";
            }
        });
        nodeC = IdentityWrapper.of(new AstNode() {
            @Override
            public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
                return null;
            }

            @Override
            public List<AstNode> children() {
                return List.of();
            }

            @Override
            public String toString() {
                return "NodeC";
            }
        });
        // Create sample edges
        Map<GGNNEdgeType, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>> edgeTypeMap = Map.of(
                GGNNEdgeType.CHILD, Set.of(Pair.of(nodeA, nodeB)),
                GGNNEdgeType.LAST_WRITE, Set.of(Pair.of(nodeB, nodeC))
        );

        // Create the graph
        Graph<IdentityWrapper<AstNode>, DefaultEdge> jgraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        jgraph.addVertex(nodeA);
        jgraph.addVertex(nodeB);
        jgraph.addVertex(nodeC);
        jgraph.addEdge(nodeA, nodeB);
        jgraph.addEdge(nodeB, nodeC);

        Set<IdentityWrapper<AstNode>> labelNodes = Set.of(nodeA, nodeB, nodeC);

        graph = new GGNNGraph("TestGraph", labelNodes, jgraph, edgeTypeMap);
    }


    @Test
    public void testGetEdgesByType() {
        Map<GGNNEdgeType, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>> edgesByType = graph.getEdgesByType();

        // Check edge types
        assertTrue(edgesByType.containsKey(GGNNEdgeType.CHILD), "Edge type CHILD should be present");
        assertTrue(edgesByType.containsKey(GGNNEdgeType.LAST_WRITE), "Edge type LAST_WRITE should be present");

        // Validate CHILD edges
        Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> childEdges = edgesByType.get(GGNNEdgeType.CHILD);
        assertEquals(1, childEdges.size(), "There should be 1 CHILD edge");
        assertTrue(childEdges.contains(Pair.of(nodeA, nodeB)), "CHILD edge from NodeA to NodeB should be present");

        // Validate LAST_WRITE edges
        Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> lastWriteEdges = edgesByType.get(GGNNEdgeType.LAST_WRITE);
        assertEquals(1, lastWriteEdges.size(), "There should be 1 LAST_WRITE edge");
        assertTrue(lastWriteEdges.contains(Pair.of(nodeB, nodeC)), "LAST_WRITE edge from NodeB to NodeC should be present");
    }

}

