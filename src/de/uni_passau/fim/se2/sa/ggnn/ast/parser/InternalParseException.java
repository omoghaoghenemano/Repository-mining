// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.parser;

import java.io.Serial;

public class InternalParseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InternalParseException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InternalParseException(final String message) {
        super(message);
    }

    public InternalParseException(final String message, final String parsedText) {
        super(message + " Tried to parse text: " + parsedText);
    }
}
