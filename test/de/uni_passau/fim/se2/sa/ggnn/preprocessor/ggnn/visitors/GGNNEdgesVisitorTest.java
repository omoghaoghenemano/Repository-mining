package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.parser.TransformationException;
import de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.GGNNEdgeType;
import de.uni_passau.fim.se2.sa.ggnn.util.SourceFixtureParser;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GGNNEdgesVisitorTest {

    private MethodDeclaration method;
    private IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap;
    private GGNNEdgesVisitor visitor;

    @BeforeEach
    public void setUp() throws URISyntaxException, IOException, TransformationException, NoSuchElementException {
        SourceFixtureParser sourceFixtureParser = new SourceFixtureParser();
        method = sourceFixtureParser.getMethodInSourceFixture("main", "ExtendedExample.java");
        astNodeMap = sourceFixtureParser.buildIdentityMap(method);

        visitor = new GGNNEdgesVisitor(method);
    }







    @Test
    public void testGetEdges() {
        Map<GGNNEdgeType, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>> edgesMap = visitor.getEdges();

        assertTrue(edgesMap.containsKey(GGNNEdgeType.CHILD));
        assertTrue(edgesMap.containsKey(GGNNEdgeType.NEXT_TOKEN));
        assertTrue(edgesMap.containsKey(GGNNEdgeType.RETURNS_TO));
        assertTrue(edgesMap.containsKey(GGNNEdgeType.LAST_WRITE));
        assertTrue(edgesMap.containsKey(GGNNEdgeType.LAST_READ));
        assertTrue(edgesMap.containsKey(GGNNEdgeType.COMPUTED_FROM));
        assertTrue(edgesMap.containsKey(GGNNEdgeType.GUARDED_BY));

        // Optionally, print the edges for debugging
        edgesMap.forEach((key, value) -> {
            System.out.println("Edge Type: " + key);
            value.forEach(edge -> System.out.println("Edge: " + edge));
        });

        // Add specific assertions based on expected edge counts and types
    }
}
