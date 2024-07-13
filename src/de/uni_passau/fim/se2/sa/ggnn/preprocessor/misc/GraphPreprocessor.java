// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.preprocessor.misc;

import com.google.common.io.Files;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.parser.AstCodeParser;
import de.uni_passau.fim.se2.sa.ggnn.ast.parser.ParseException;
import de.uni_passau.fim.se2.sa.ggnn.preprocessor.CommonPreprocessorOptions;
import de.uni_passau.fim.se2.sa.ggnn.preprocessor.Preprocessor;
import de.uni_passau.fim.se2.sa.ggnn.preprocessor.shared.AstWithLabels;
import de.uni_passau.fim.se2.sa.ggnn.preprocessor.shared.CodeAndLabels;
import de.uni_passau.fim.se2.sa.ggnn.preprocessor.shared.CodeAndLabelsExtractor;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class GraphPreprocessor extends Preprocessor {

    private static final Logger log = LoggerFactory.getLogger(GraphPreprocessor.class);

    private final AstCodeParser parser = new AstCodeParser();
    private final boolean singleMethod;
    private final String outputFileExtension;

    protected GraphPreprocessor(
        CommonPreprocessorOptions commonOptions, boolean singleMethod, String outputFileExtension
    ) {
        super(commonOptions);

        this.singleMethod = singleMethod;
        this.outputFileExtension = outputFileExtension;
    }

    protected Path getOutputFile(final Path input) {
        final String withoutExt = Files.getNameWithoutExtension(input.toString());
        final Path outputFileName = Path.of(withoutExt + "." + outputFileExtension);

        if (commonOptions.outputPath().isDirectory()) {
            return getOutputFileInDirectory(input, outputFileName);
        }
        else {
            return outputFileName;
        }
    }

    private Path getOutputFileInDirectory(final Path input, final Path outputFileName) {
        if (commonOptions.inputPath().isConsole()) {
            return outputFileName;
        }

        final Path relativeInputFile = commonOptions.inputPath().getPath().relativize(input);
        if (relativeInputFile.getParent() == null) {
            return outputFileName;
        }
        else {
            return relativeInputFile.getParent().resolve(outputFileName);
        }
    }

    protected AstNode getAst(final String code) throws ParseException {
        if (singleMethod) {
            return parser.parseMethod(code);
        }
        else {
            return parser.parseCodeToCompilationUnit(code);
        }
    }

    protected Optional<AstNode> getAstMethods(final String methodCode) {
        try {
            return Optional.of(parser.parseMethod(methodCode));
        }
        catch (ParseException e) {
            log.warn("Could not parse \"{}\".", methodCode.replace("\n", "\\n"));
            return Optional.empty();
        }
    }

    protected Optional<Pair<Path, Stream<AstWithLabels>>> processInput(final Input input) {
        try {
            final Path outputFile = getOutputFile(input.file());
            final AstNode ast = getAst(input.content());
            return Optional.of(Pair.of(outputFile, Stream.of(new AstWithLabels(Collections.emptyList(), ast))));
        }
        catch (ParseException e) {
            log.warn("Could not parse {}.", input.file(), e);
            return Optional.empty();
        }
    }

    private Optional<Pair<Path, Stream<AstWithLabels>>> processToLabelledAst(Input input, Path outputFile) {
        final Stream<CodeAndLabels> codeAndLabels = CodeAndLabelsExtractor
            .parseJSONLinesFile(input.file().toAbsolutePath());
        final Stream<AstWithLabels> labels = codeAndLabels
            .map(x -> Pair.of(x.labels(), getAstMethods(x.methodCode()))).filter(x -> x.b().isPresent())
            .map(codeLabel -> new AstWithLabels(codeLabel.a(), codeLabel.b().get()));
        return Optional.of(Pair.of(outputFile, labels));
    }

    protected Stream<Pair<Path, AstWithLabels>> flatten(Pair<Path, Stream<AstWithLabels>> pair) {
        return pair.b().map(x -> Pair.of(pair.a(), x));
    }
}
