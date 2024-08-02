package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.parser.TransformationException;
import de.uni_passau.fim.se2.sa.ggnn.util.SourceFixtureParser;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GuardedByVisitorTest {

    private MethodDeclaration method;
    private IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap;
    private GuardedByVisitor visitor;

    @BeforeEach
    public void setUp() throws URISyntaxException, IOException, TransformationException, NoSuchElementException {
        SourceFixtureParser sourceFixtureParser = new SourceFixtureParser();
        method = sourceFixtureParser.getMethodInSourceFixture("main", "ExtendedExample.java");
        astNodeMap = sourceFixtureParser.buildIdentityMap(method);

        visitor = new GuardedByVisitor(astNodeMap);
    }

    @Test
    public void testGuardedByEdges() {
        Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> edges = new HashSet<>();
        method.accept(visitor, edges);

        // Print the edges for debugging
        edges.forEach(edge -> System.out.println("Edge: " + edge));

        // Adjust the expected number of edges based on correct analysis
        assertEquals(5, edges.size(), "Expected 5 edges but found " + edges.size());
        assertTrue(edges.size() > 0, "No edges were created");
    }
}