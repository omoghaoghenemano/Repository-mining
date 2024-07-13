// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.parser;

import java.io.Serial;

public class TransformationException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public TransformationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TransformationException(final String message) {
        super(message);
    }

    public TransformationException(final String message, final String parsedText) {
        super(message + " Tried to process text: " + parsedText);
    }
}
