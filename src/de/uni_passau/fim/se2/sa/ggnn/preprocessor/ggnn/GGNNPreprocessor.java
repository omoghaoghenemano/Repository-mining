// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.sa.ggnn.preprocessor.CommonPreprocessorOptions;
import de.uni_passau.fim.se2.sa.ggnn.preprocessor.ProcessingException;
import de.uni_passau.fim.se2.sa.ggnn.preprocessor.misc.GraphPreprocessor;
import de.uni_passau.fim.se2.sa.ggnn.preprocessor.shared.AstWithLabels;
import de.uni_passau.fim.se2.sa.ggnn.preprocessor.shared.MethodsExtractor;
import de.uni_passau.fim.se2.sa.ggnn.util.dotgraph.DotGraph;
import de.uni_passau.fim.se2.sa.ggnn.util.dotgraph.DotGraphBuildingVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * GGNN Preprocessor
 */
public class GGNNPreprocessor extends GraphPreprocessor {

    private static final Logger log = LoggerFactory.getLogger(GGNNPreprocessor.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final boolean dotgraph;

    public GGNNPreprocessor(CommonPreprocessorOptions commonOptions, boolean singleMethod, boolean dotgraph) {
        super(commonOptions, singleMethod, dotgraph ? "dot" : "jsonl");
        this.dotgraph = dotgraph;
    }

    @Override
    public void process() throws ProcessingException {
        writeResult(
            readInputs().flatMap(input -> processInput(input).stream())
                .flatMap(this::flatten)
                .map(p -> p.mapB(this::process))
                .filter(p -> p.b().isPresent())
                .map(p -> p.mapB(Optional::get))
        );
    }

    @Override
    public Stream<String> processCompilationUnit(final String code) {
        return processCode(code);
    }

    @Override
    public Optional<String> processSingleMethod(final String code) {
        return processCode(code).findFirst();
    }

    private Stream<String> processCode(final String code) {
        return processSingleElement(code)
            .map(y -> new AstWithLabels(Collections.emptyList(), y))
            .map(this::process)
            .flatMap(Optional::stream);
    }

    /**
     * Preprocess given content to a string either representing a DotGraph or a JSON format.
     *
     * @param root Root of the AST.
     * @return Returns either JSON or DotGraph format of the GGNN Graph.
     */
    private Optional<String> process(final AstWithLabels root) {
        if (dotgraph) {
            final var v = new DotGraphBuildingVisitor<>(new GGNNGraphBuilder());
            return root.astNode().accept(v, null).findFirst().map(DotGraph::build);
        }
        else {
            final var methodDecls = new MethodsExtractor(false).process(root.astNode());
            final var jsonGraphs = methodDecls.stream()
                .map(methodDeclaration -> new GGNNGraphBuilder().build(methodDeclaration))
                .map(GGNNContextGraphBuilder::build)
                .flatMap(x -> graphAsJsonStream(x, root.newLabels()))
                .collect(Collectors.joining(System.lineSeparator()));
            return Optional.of(jsonGraphs);
        }
    }

    private Stream<String> graphAsJsonStream(final GGNNContextGraph contextGraph, List<String> newLabels) {
        if (newLabels.isEmpty()) {
            return Stream.of(graphAsJson(contextGraph)).filter(Objects::nonNull);
        }
        return newLabels.stream()
            .map(label -> new GGNNContextGraph(label, contextGraph.labelNodes(), contextGraph.contextGraph()))
            .map(this::graphAsJson)
            .filter(Objects::nonNull);
    }

    private String graphAsJson(final GGNNContextGraph contextGraph) {
        try {
            return objectMapper.writeValueAsString(contextGraph);
        }
        catch (JsonProcessingException ex) {
            // as long as the classes are accessible to Jackson this should never happen
            log.error("Could not transform statement tree sequence to JSON.", ex);
            return null;
        }
    }

}
