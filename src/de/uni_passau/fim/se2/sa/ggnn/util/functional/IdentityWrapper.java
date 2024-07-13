// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.util.functional;

import com.google.common.base.Preconditions;

public class IdentityWrapper<T> {

    private final T elem;

    protected IdentityWrapper(T elem) {
        Preconditions.checkNotNull(elem);
        this.elem = elem;
    }

    public static <T> IdentityWrapper<T> of(final T elem) {
        return new IdentityWrapper<>(elem);
    }

    public T elem() {
        return elem;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        else if (obj instanceof IdentityWrapper<?> that) {
            // explicitly use == instead of equals to check for true reference
            // equality
            return that.elem == this.elem;
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return elem.hashCode();
    }

    @Override
    public String toString() {
        return "Id(" + elem.toString() + ")";
    }
}
