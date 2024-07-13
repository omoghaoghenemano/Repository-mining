// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model;

import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Foldable;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.TraversalOrder;

import java.util.List;
import java.util.stream.Stream;

public interface AstNode extends Visitable, Foldable<AstNode> {

    List<AstNode> children();

    default boolean isLeaf() {
        return children().isEmpty();
    }

    @Override
    default Stream<AstNode> fold(TraversalOrder order) {
        final Stream<AstNode> children = children().stream().flatMap(c -> c.fold(order));
        return switch (order) {
            case PRE_ORDER -> Stream.concat(Stream.of(this), children);
            case POST_ORDER -> Stream.concat(children, Stream.of(this));
        };
    }
}
