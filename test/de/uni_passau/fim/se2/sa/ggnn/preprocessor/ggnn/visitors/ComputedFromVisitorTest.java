package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.Expression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.binary.AssignmentExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.binary.BinaryExpr;
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

public class ComputedFromVisitorTest {

    private AstNode root;
    private IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap;
    private ComputedFromVisitor visitor;

    @BeforeEach
    public void setUp() throws URISyntaxException, IOException, TransformationException {
        SourceFixtureParser sourceFixtureParser = new SourceFixtureParser();
        root = sourceFixtureParser.getMethodInSourceFixture("main", "ExtendedExample.java");
        astNodeMap = sourceFixtureParser.buildIdentityMap(root);

        visitor = new ComputedFromVisitor(astNodeMap);
    }

    @Test
    public void testComputedFromEdges() {
        Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> computedFromEdges = new HashSet<>();
        root.accept(visitor, computedFromEdges);

        // Print the result for debugging
        computedFromEdges.forEach(edge -> System.out.println("Computed From Edge: " + edge));

        // Adjust the expected number of edges based on real data
        assertTrue(computedFromEdges.size() > 0, "No COMPUTED_FROM edges were created");

        // Example assertions for specific edges
        // Replace these with actual nodes and expected results based on your test data
        // IdentityWrapper<AstNode> expectedSource = astNodeMap.get(someSourceNode);
        // IdentityWrapper<AstNode> expectedTarget = astNodeMap.get(someTargetNode);
        // assertTrue(computedFromEdges.contains(new Pair<>(expectedSource, expectedTarget)), "Expected computed from edge not found");
    }
}
