// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.util.functional;

import java.util.Objects;
import java.util.function.Function;

/**
 * A pair of two guaranteed non-null values.
 *
 * @param a   The first value.
 * @param b   The second value.
 * @param <A> The type of the first value.
 * @param <B> The type of the second value.
 */
public record Pair<A, B>(A a, B b) {

    public Pair(final A a, final B b) {
        this.a = Objects.requireNonNull(a, "Pair elements cannot be null.");
        this.b = Objects.requireNonNull(b, "Pair elements cannot be null.");
    }

    public static <A, B> Pair<A, B> of(final A a, final B b) {
        return new Pair<>(a, b);
    }

    public Pair<B, A> swap() {
        return new Pair<>(b, a);
    }

    public <C, D> Pair<C, D> map2(final Function<A, C> mapA, final Function<B, D> mapB) {
        return this.mapA(mapA).mapB(mapB);
    }

    public <C> Pair<C, B> mapA(final Function<A, C> map) {
        return new Pair<>(map.apply(a), b);
    }

    public <C> Pair<A, C> mapB(final Function<B, C> map) {
        return new Pair<>(a, map.apply(b));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        else if (o instanceof Pair<?, ?> pair) {
            return a.equals(pair.a) && b.equals(pair.b);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    @Override
    public String toString() {
        return "(" + a + ", " + b + ")";
    }
}
