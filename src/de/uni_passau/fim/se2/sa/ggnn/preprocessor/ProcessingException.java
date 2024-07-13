// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.preprocessor;

import java.io.Serial;

public class ProcessingException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
