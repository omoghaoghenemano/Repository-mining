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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChildVisitorTest {

    private MethodDeclaration method;
    private IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap;
    private ChildVisitor visitor;

    @BeforeEach
    public void setUp() throws URISyntaxException, IOException, TransformationException {
        SourceFixtureParser sourceFixtureParser = new SourceFixtureParser();
        method = sourceFixtureParser.getMethodInSourceFixture("main", "ExtendedExample.java");
        astNodeMap = sourceFixtureParser.buildIdentityMap(method);

        visitor = new ChildVisitor(astNodeMap);
    }

    @Test
    public void testChildEdges() {
        Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> childEdges = new HashSet<>();
        method.accept(visitor, childEdges);

        // Print the result for debugging
        childEdges.forEach(edge -> System.out.println("Child Edge: " + edge));

        // Adjust the expected number of edges based on real data
        assertTrue(childEdges.size() > 0, "No CHILD edges were created");

        // Example assertions
        // Check specific edges if you know them
        // IdentityWrapper<AstNode> parent = astNodeMap.get(someParentNode);
        // IdentityWrapper<AstNode> child = astNodeMap.get(someChildNode);
        // assertTrue(childEdges.contains(new Pair<>(parent, child)), "Expected child edge not found");
    }
}
