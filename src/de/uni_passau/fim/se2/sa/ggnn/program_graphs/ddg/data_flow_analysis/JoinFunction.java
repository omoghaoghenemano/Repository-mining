// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg.data_flow_analysis;

import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface JoinFunction<T> extends Function<Set<Set<T>>, Set<T>> {

    final class MustFunction<T> implements JoinFunction<T> {

        @Override
        public Set<T> apply(Set<Set<T>> sets) {
            return sets.stream().reduce(Sets::intersection).orElse(Collections.emptySet());
        }
    }

    final class MayFunction<T> implements JoinFunction<T> {

        @Override
        public Set<T> apply(Set<Set<T>> sets) {
            return sets.stream().flatMap(Set::stream).collect(Collectors.toSet());
        }
    }
}
