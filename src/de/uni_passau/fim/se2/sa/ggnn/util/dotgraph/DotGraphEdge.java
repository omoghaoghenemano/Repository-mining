// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.util.dotgraph;

import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.util.Optional;

public record DotGraphEdge<A>(Pair<A, A> edge, Optional<String> colour) {

    public DotGraphEdge(final Pair<A, A> edge) {
        this(edge, Optional.empty());
    }

    public DotGraphEdge(final Pair<A, A> edge, final String colour) {
        this(edge, Optional.of(colour));
    }
}
