// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.preprocessor.shared;


import de.uni_passau.fim.se2.sa.ggnn.preprocessor.ProcessingException;

import java.util.Objects;

@FunctionalInterface
public interface ProcessingStep<I, O> {

    O process(I input) throws ProcessingException;

    default <V> ProcessingStep<V, O> compose(ProcessingStep<? super V, ? extends I> before) {
        Objects.requireNonNull(before);
        return (V v) -> process(before.process(v));
    }

    default <V> ProcessingStep<I, V> andThen(ProcessingStep<? super O, ? extends V> after) {
        Objects.requireNonNull(after);
        return (I t) -> after.process(process(t));
    }
}
