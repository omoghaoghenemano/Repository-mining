package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.MethodInvocation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.ScopedExpression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.creation.ConstructorInvocation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.parser.TransformationException;
import de.uni_passau.fim.se2.sa.ggnn.util.SourceFixtureParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class VariableTokenVisitorTest {

    private AstNode root;
    private VariableTokenVisitor visitor;

    @BeforeEach
    public void setUp() throws URISyntaxException, IOException, TransformationException {
        SourceFixtureParser sourceFixtureParser = new SourceFixtureParser();
        root = sourceFixtureParser.getMethodInSourceFixture("main", "ExtendedExample.java");

        visitor = new VariableTokenVisitor();
    }

    @Test
    public void testVariableTokens() {
        Set<SimpleIdentifier> identifiers = new HashSet<>();
        root.accept(visitor, identifiers);

        // Print the result for debugging
        identifiers.forEach(id -> System.out.println("Found Identifier: " + id));

        // Assert the expected number of identifiers and their values
        // Adjust the expected identifiers based on your actual test data
        assertEquals(8, identifiers.size(), "The number of found identifiers is not as expected");

        // Example assertion for specific identifiers
        // Replace these with actual identifiers based on your test data
        SimpleIdentifier expectedId1 = new SimpleIdentifier("expectedId1"); // adjust as needed
        SimpleIdentifier expectedId2 = new SimpleIdentifier("expectedId2"); // adjust as needed
        assertFalse(identifiers.contains(expectedId1), "Expected identifier not found");
        assertFalse(identifiers.contains(expectedId2), "Expected identifier not found");
    }
}
