// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2
package de.uni_passau.fim.se2.sa.ggnn.preprocessor.shared;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_passau.fim.se2.sa.ggnn.util.FileReadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public final class CodeAndLabelsExtractor {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(CodeAndLabelsExtractor.class);

    private CodeAndLabelsExtractor() {
        throw new IllegalCallerException("Utility class!");
    }

    /**
     * Parses a jsonl file line to a {@link CodeAndLabels} object.
     *
     * @param inputPath The given input path where the {@link CodeAndLabels} should be parsed.
     * @return A Stream containing all successfully parsed {@link CodeAndLabels}.
     */
    public static Stream<CodeAndLabels> parseJSONLinesFile(Path inputPath) {
        try {
            return FileReadUtil.readFileWithBomLines(inputPath.toFile())
                .map(CodeAndLabelsExtractor::parseLine)
                .flatMap(Optional::stream);
        }
        catch (IOException e) {
            log.error("The given input path does not exist!", e);
            return Stream.empty();
        }
    }

    /**
     * Parses a json line to a {@link CodeAndLabels} object. If parsing was not successfully, a log message is printed.
     *
     * @param line The given line that should be parsed.
     * @return An optional containing the parsed {@link CodeAndLabels}. In case of an unparsable line, an empty optional
     *         is returned.
     */
    public static Optional<CodeAndLabels> parseLine(String line) {
        try {
            return Optional.of(MAPPER.readValue(line, CodeAndLabels.class));
        }
        catch (JsonProcessingException e) {
            log.warn("Line \"{}\" couldn't be parsed to type CodeAndLabels", line);
            return Optional.empty();
        }
    }
}
