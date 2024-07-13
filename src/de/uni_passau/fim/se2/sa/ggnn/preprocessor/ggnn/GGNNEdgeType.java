// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn;

/**
 * All edge types of the GGNN paper. LAST_READ and LAST_WRITE are both encoded in DATA_DEPENDENCY. FORMAL_ARGS is not
 * presented because currently our implementation only focus on intra procedural analysis.
 */
public enum GGNNEdgeType {
    CHILD,
    NEXT_TOKEN,
    LAST_WRITE,
    LAST_READ,
    COMPUTED_FROM,
    GUARDED_BY,
    RETURNS_TO
}
