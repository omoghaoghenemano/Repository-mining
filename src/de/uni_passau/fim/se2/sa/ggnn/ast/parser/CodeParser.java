// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.parser;

import de.uni_passau.fim.se2.sa.ggnn.javaparser.JavaLexer;
import de.uni_passau.fim.se2.sa.ggnn.javaparser.JavaParser;
import org.antlr.v4.runtime.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeParser {

    private static final Logger log = LoggerFactory.getLogger(CodeParser.class);

    /**
     * Takes a code fragment string and returns the java parser object.
     *
     * @param code The code string.
     * @return The java parser object.
     */
    public JavaParser parseCodeFragment(String code) {
        final JavaLexer lx = new JavaLexer(CharStreams.fromString(code));
        final CommonTokenStream tokenStream = new CommonTokenStream(lx);
        final JavaParser parser = new JavaParser(tokenStream);
        parser.getErrorListeners().clear();
        parser.addErrorListener(new ErrorListener());
        return parser;
    }

    private static class ErrorListener extends ConsoleErrorListener {

        @Override
        public void syntaxError(
            Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
            String msg, RecognitionException e
        ) {
            log.warn("Parse error in line {} column {}: {}", line, charPositionInLine, msg);
        }
    }

}
