// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.parser;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.shared.JavaFileTransformation;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Extends the {@link JavaFileTransformation} for parsing compilation units to {@link AstNode}.
 */
public class AstParserWrapper extends JavaFileTransformation<AstNode> {

    /**
     * Creates an instance of this class.
     *
     * @param sourceDirectory The source directory.
     * @param printProgress   Indicates if the progress should be printed.
     * @throws IOException In case of any input/output failures.
     */

    public AstParserWrapper(Path sourceDirectory, boolean printProgress)
        throws IOException {
        super(sourceDirectory, printProgress, code -> new AstCodeParser().parseCodeToCompilationUnit(code));
    }

    /**
     * Creates an instance of this class.
     *
     * @param sourceDirectory The source directory.
     * @throws IOException In case of any input/output failures.
     */
    public AstParserWrapper(final Path sourceDirectory) throws IOException {
        this(sourceDirectory, false);
    }
}
