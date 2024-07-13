// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.preprocessor;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.parser.AstCodeParser;
import de.uni_passau.fim.se2.sa.ggnn.preprocessor.shared.CodeAndLabelsExtractor;
import de.uni_passau.fim.se2.sa.ggnn.util.FileReadUtil;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Common superclass of all preprocessors.
 * <p>
 * Provides shared functionality like reading from the input and writing to the output.
 */
public abstract class Preprocessor {

    private static final Logger log = LoggerFactory.getLogger(Preprocessor.class);

    private static final Path DEFAULT_OUTPUT_FILENAME = Path.of("result.txt");

    private static final String NEWLINE = System.lineSeparator();

    protected static final Path CONSOLE_PATH = Path.of("CONSOLE");

    protected final CommonPreprocessorOptions commonOptions;

    protected Preprocessor(final CommonPreprocessorOptions commonOptions) {
        this.commonOptions = commonOptions;
    }

    public abstract void process() throws ProcessingException;

    /**
     * Preprocesses the code of a compilation unit (i.e. a Java file).
     *
     * @param code The code to be processed.
     * @return The preprocessed code.
     */
    public abstract Stream<String> processCompilationUnit(String code);

    /**
     * Preprocesses the code of a single method and converts it to the scheme of the corresponding model.
     *
     * @param code The code to be preprocessed.
     * @return The preprocessed code if the preprocessing could be applied.
     */
    public abstract Optional<String> processSingleMethod(String code);

    protected record Input(Path file, String content) {
    }

    /**
     * Reads all inputs from the requested sources.
     * <p>
     * For an input directory, recursively walks over all files. Otherwise, reads the content from a single file or the
     * console input. For console input, the file path of the element is {@link #CONSOLE_PATH}.
     *
     * @return One {@link Input} element for each file in the predefined source.
     * @throws ProcessingException Thrown if reading from a file failed.
     */
    protected final Stream<Input> readInputs() throws ProcessingException {
        return switch (commonOptions.inputPath().getPathType()) {
            case CONSOLE -> Stream.of(readConsoleInput());
            case DIRECTORY -> readDirectoryFiles(commonOptions.inputPath().getPath());
            case FILE -> tryReadFileInput(commonOptions.inputPath().getPath()).stream();
        };
    }

    private Input readConsoleInput() throws ProcessingException {
        try (var is = new InputStreamReader(System.in, StandardCharsets.UTF_8); var br = new BufferedReader(is)) {
            final String input = br.lines().collect(Collectors.joining(NEWLINE));
            return new Input(CONSOLE_PATH, input.trim());
        }
        catch (IOException e) {
            throw new ProcessingException("Cannot read from stdin.", e);
        }
    }

    private Stream<Input> readDirectoryFiles(final Path directory) throws ProcessingException {
        try (var files = Files.walk(directory)) {
            return files
                .filter(p -> p.toFile().isFile())
                .toList()
                .stream()
                .map(this::tryReadFileInput)
                .flatMap(Optional::stream);
        }
        catch (IOException e) {
            throw new ProcessingException("Could not read all contents from directory " + directory, e);
        }
    }

    private Optional<Input> tryReadFileInput(final Path file) {
        try {
            final String content = FileReadUtil.readFileWithBom(file.toFile());
            return Optional.of(new Input(file, content));
        }
        catch (IOException e) {
            log.warn("Could not read from file: {}", file, e);
            return Optional.empty();
        }
    }

    /**
     * Writes a single output to the predefined destination.
     *
     * @param output The content that should be written.
     * @throws ProcessingException Thrown in case writing to a file failed.
     */
    protected final void writeResult(final String output) throws ProcessingException {
        switch (commonOptions.outputPath().getPathType()) {
            case CONSOLE -> writeResultsToConsole(Stream.of(output));
            case FILE -> writeResultsToFile(commonOptions.outputPath().getPath(), Stream.of(output));
            case DIRECTORY -> writeResultsToFile(DEFAULT_OUTPUT_FILENAME, Stream.of(output));
        }
    }

    /**
     * Writes multiple elements to the predefined destination.
     * <p>
     * Writes all elements to a single destination.
     *
     * @param elements THe contents that should be written.
     * @throws ProcessingException Thrown in case writing to a file failed.
     */
    protected final void writeResults(final Stream<String> elements) throws ProcessingException {
        switch (commonOptions.outputPath().getPathType()) {
            case CONSOLE -> writeResultsToConsole(elements);
            case FILE -> writeResultsToFile(commonOptions.outputPath().getPath(), elements);
            case DIRECTORY -> writeResultsToFile(DEFAULT_OUTPUT_FILENAME, elements);
        }
    }

    /**
     * Writes a single output to a file.
     *
     * @param filename To which the output should be written. If the target is a directory, it is resolved relative to
     *                 that directory. Ignored, if the output was requested on the console.
     * @param output   The content that should be written.
     * @throws ProcessingException Thrown in case writing to a file failed.
     */
    protected void writeResult(final Path filename, final String output) throws ProcessingException {
        switch (commonOptions.outputPath().getPathType()) {
            case CONSOLE -> writeResultsToConsole(Stream.of(output));
            case FILE -> writeResultsToFile(commonOptions.outputPath().getPath(), Stream.of(output));
            case DIRECTORY -> writeResultsToFile(filename, Stream.of(output));
        }
    }

    /**
     * Writes outputs to separate files.
     *
     * @param outputFiles To which the outputs should be written. If the output is a single file or the console, all
     *                    contents are appended into this single target.
     * @throws ProcessingException Thrown in case writing to a file failed.
     */
    protected final void writeResult(final Stream<Pair<Path, String>> outputFiles) throws ProcessingException {
        switch (commonOptions.outputPath().getPathType()) {
            case CONSOLE -> writeResultsToConsole(outputFiles.map(Pair::b));
            case FILE -> {
                final Path outputPath = commonOptions.outputPath().getPath();
                log.warn(
                    "Preprocessor requested to write multiple files, but the output is specified as a single file. Writing everything to {}.",
                    outputPath
                );
                writeResultsToFile(outputPath, outputFiles.map(Pair::b));
            }
            case DIRECTORY -> writeResultsToFiles(outputFiles);
        }
    }

    private void writeResultsToConsole(final Stream<String> results) {
        results.forEach(System.out::println);
    }

    private void writeResultsToFiles(final Stream<Pair<Path, String>> results) throws ProcessingException {
        final Iterator<Pair<Path, String>> resultsIt = results.iterator();
        while (resultsIt.hasNext()) {
            final Pair<Path, String> result = resultsIt.next();
            writeResultsToFile(result.a(), Stream.of(result.b()));
        }
    }

    private void writeResultsToFile(final Path filename, final Stream<String> results) throws ProcessingException {
        final Path outputFile = getOutputFilePath(filename);
        createOutputDirectories(outputFile);

        try (
            var os = Files.newOutputStream(outputFile);
            var pw = new PrintWriter(os, true, StandardCharsets.UTF_8)
        ) {
            results.forEach(pw::println);
        }
        catch (IOException e) {
            throw new ProcessingException("Cannot write to output file " + outputFile, e);
        }
    }

    private Path getOutputFilePath(final Path filename) {
        if (commonOptions.outputPath().isDirectory()) {
            return commonOptions.outputPath().getPath().resolve(filename);
        }
        else {
            return filename;
        }
    }

    private void createOutputDirectories(final Path filename) throws ProcessingException {
        final Path directory = filename.getParent();
        if (directory == null) {
            return;
        }

        try {
            Files.createDirectories(directory);
        }
        catch (IOException e) {
            throw new ProcessingException("Cannot write to output file " + filename, e);
        }
    }

    protected Stream<AstNode> processSingleElement(final String code)
        throws ProcessingException {
        final AstCodeParser codeParser = new AstCodeParser();
        return codeParser.parseMethodSkipErrors(code).stream().map(AstNode.class::cast);
    }

    /**
     * Loads labels/code pairs from the input JSON Lines file.
     *
     * @return One labels/code pair for each parseable line in the input.
     */
    protected Stream<Pair<List<String>, AstNode>> loadLabelledCode() {
        return CodeAndLabelsExtractor.parseJSONLinesFile(commonOptions.inputPath().getPath())
            .map(x -> Pair.of(x.labels(), processSingleElement(x.methodCode())))
            .map(pair -> pair.mapB(Stream::findFirst))
            .filter(pair -> pair.b().isPresent())
            .map(pair -> pair.mapB(Optional::orElseThrow));
    }
}
