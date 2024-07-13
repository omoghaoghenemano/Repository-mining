// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2
package de.uni_passau.fim.se2.sa.ggnn.preprocessor.shared;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * DTO that contains the code of a method and all corresponding labels.
 */
public record CodeAndLabels(@JsonProperty("method_code") String methodCode, List<String> labels)
    implements Serializable {

    @Serial
    private static final long serialVersionUID = 0L;
}
