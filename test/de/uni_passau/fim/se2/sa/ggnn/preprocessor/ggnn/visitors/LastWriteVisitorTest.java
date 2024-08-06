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

public class LastWriteVisitorTest {

    private MethodDeclaration method;
    private IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap;
    private DataFlowFacts dataFlowFacts;
    private LastWriteVisitor visitor;

    @BeforeEach
    public void setUp() throws URISyntaxException, IOException, TransformationException {
        SourceFixtureParser sourceFixtureParser = new SourceFixtureParser();
        method = sourceFixtureParser.getMethodInSourceFixture("main", "ExtendedExample.java");
        astNodeMap = sourceFixtureParser.buildIdentityMap(method);

        // Create DataFlowFacts with real data
        dataFlowFacts = new DataFlowFacts(method);

        // Populate DataFlowFacts with definitions and uses
        // These should be based on real data from your source files
        // For example:
        // Definition def1 = new Definition(someAstNode);
        // Use use1 = new Use(someOtherAstNode);
        // dataFlowFacts.addDefinition(def1);
        // dataFlowFacts.addUse(use1);
        // dataFlowFacts.addDefUsePair(new Pair<>(def1, use1));

        visitor = new LastWriteVisitor(astNodeMap, dataFlowFacts);
    }

    @Test
    public void testVisit() {
        Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> result = new HashSet<>();
        method.accept(visitor, result);

        // Print the result for debugging
        result.forEach(edge -> System.out.println("Last Write Edge: " + edge));

        // Adjust the expected number of edges based on real data
        assertTrue(result.size() > 0, "No LAST_WRITE edges were created");

        // You can add more specific assertions based on known edges
        // For example:
        // IdentityWrapper<AstNode> expectedDef = astNodeMap.get(someExpectedDefNode);
        // IdentityWrapper<AstNode> expectedUse = astNodeMap.get(someExpectedUseNode);
        // assertTrue(result.contains(new Pair<>(expectedDef, expectedUse)), "Expected edge not found");
    }
}
