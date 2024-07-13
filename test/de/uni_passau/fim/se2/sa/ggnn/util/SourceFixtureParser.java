package de.uni_passau.fim.se2.sa.ggnn.util;


import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.parser.AstParserWrapper;
import de.uni_passau.fim.se2.sa.ggnn.ast.parser.TransformationException;
import de.uni_passau.fim.se2.sa.ggnn.preprocessor.shared.MethodsExtractor;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public final class SourceFixtureParser {

    /**
     * Maps AstNodes to their IdentityWrapper wrapped representation.
     */
    private final IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap = new IdentityHashMap<>();


    /**
     * Tries to find a method with the given name in a fixture source file.
     *
     * @param methodName The name of the method.
     * @param fileName   The source fixture that is parsed and searched for the method.
     * @return The method if found.
     * @throws URISyntaxException     If the filename is not a valid path.
     * @throws IOException            If the content of the file could not be read.
     * @throws NoSuchElementException If no method with the given name could be found in the file.
     */
    public MethodDeclaration getMethodInSourceFixture(String methodName, String fileName)
            throws URISyntaxException, IOException, TransformationException, NoSuchElementException {
        final Path filePath = buildPath(fileName);
        final AstNode root = visitCompilationUnit(filePath);
        return new MethodsExtractor(false)
                .process(root)
                .stream()
                .filter(method -> method.name().name().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Method " + methodName + " not found in file " + filePath));
    }

    private AstNode visitCompilationUnit(Path path) throws IOException, TransformationException {
        final AstParserWrapper parser = new AstParserWrapper(path.getParent(), false);
        return parser.parseCompilationUnit(path);
    }

    private Path buildPath(String pathParts) {
        return Path.of(this.getClass().getClassLoader().getResource(pathParts).getPath());
    }


    public IdentityHashMap<AstNode, IdentityWrapper<AstNode>> buildIdentityMap(AstNode node) {
        astNodeMap.putIfAbsent(node, IdentityWrapper.of(node));
        node.children().forEach(this::buildIdentityMap);
        return astNodeMap;
    }
}
