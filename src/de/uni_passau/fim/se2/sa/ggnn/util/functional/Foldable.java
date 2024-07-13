// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.util.functional;

import java.util.stream.Stream;

@FunctionalInterface
public interface Foldable<T> {

    /**
     * Folds the tree using preorder traversal.
     *
     * @return A sequence of all nodes in the tree.
     */
    default Stream<T> fold() {
        return fold(TraversalOrder.PRE_ORDER);
    }

    Stream<T> fold(TraversalOrder order);
}
