package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.parser.TransformationException;

import de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg.DataFlowFacts;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg.Definition;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg.Use;
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

public class LastReadVisitorTest {

    private MethodDeclaration method;
    private IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap;
    private DataFlowFacts dataFlowFacts;
    private LastReadVisitor visitor;

    @BeforeEach
    public void setUp() throws URISyntaxException, IOException, TransformationException {
        SourceFixtureParser sourceFixtureParser = new SourceFixtureParser();
        method = sourceFixtureParser.getMethodInSourceFixture("main", "ExtendedExample.java");
        astNodeMap = sourceFixtureParser.buildIdentityMap(method);

        // Create DataFlowFacts with real data
        dataFlowFacts = new DataFlowFacts(method);
        // Assume dataFlowFacts can be initialized here with real definitions and uses
        // Example: dataFlowFacts.addDefinition(...);
        // Example: dataFlowFacts.addUse(...);

        visitor = new LastReadVisitor(astNodeMap, dataFlowFacts);
    }

    @Test
    public void testVisit() {
        Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> result = new HashSet<>();
        method.accept(visitor, result);

        // Print the result for debugging
        result.forEach(edge -> System.out.println("Last Read Edge: " + edge));

        // Adjust the expected number of edges and values based on real data
        assertTrue(result.size() > 0, "No LAST_READ edges were created");

        // If you know the exact expected edges, assert them explicitly
        // Example:
        // IdentityWrapper<AstNode> expectedDef = astNodeMap.get(...);
        // IdentityWrapper<AstNode> expectedUse = astNodeMap.get(...);
        // assertTrue(result.contains(Pair.of(expectedDef, expectedUse)), "Expected edge not found");
    }
}
