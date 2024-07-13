// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.shared;

import de.uni_passau.fim.se2.sa.ggnn.ast.parser.ParseException;
import de.uni_passau.fim.se2.sa.ggnn.ast.parser.TransformationException;

/**
 * Creating own parse function. We use an own variant instead of {@link java.util.function.Function}, because we need
 * the {@link ParseException} declaration.
 *
 * @param <T> The input type.
 * @param <R> The output type.
 */
public interface TransformationFunction<T, R> {

    /**
     * Gets an object of type {@link T} and returns something of type {@link R}.
     *
     * @param t The input object.
     * @return Some parsed result.
     * @throws TransformationException In case of parse failures.
     */
    R apply(T t) throws TransformationException;

}
