// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs;

import java.util.function.Function;

@FunctionalInterface
public interface Builder<I, O> extends Function<I, O> {

    O build(I input);

    @Override
    default O apply(I input) {
        return build(input);
    }
}
