// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model;

import de.uni_passau.fim.se2.sa.ggnn.ast.parser.InternalParseException;

public interface TryFromString<T> {

    static <A> InternalParseException fromStringFailure(final String fromStr, final Class<A> targetClass) {
        return new InternalParseException("Could not convert '" + fromStr + "' to a " + targetClass.getSimpleName());
    }
}
