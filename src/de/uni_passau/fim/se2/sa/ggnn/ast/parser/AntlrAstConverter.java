// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.parser;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNodeList;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

interface AntlrAstConverter {

    default <I, O> Optional<O> visitNullable(final I input, final Function<I, O> visit) {
        return Optional.ofNullable(input).map(visit);
    }

    default <I, O> List<O> visitNullableList(final List<I> list, final Function<I, O> visit) {
        if (list == null) {
            return Collections.emptyList();
        }
        else {
            return list.stream().map(visit).toList();
        }
    }

    default void checkErrorNode(String element, ParserRuleContext ctx) throws InternalParseException {
        if (ctx == null) {
            throw new InternalParseException("Could not parse " + element);
        }
        else if (ctx.children == null) {
            final String msg = String.format("Could not parse %s '%s'!", element, ctx.getText());
            throw new InternalParseException(msg);
        }

        ctx.children.stream()
            .filter(ErrorNode.class::isInstance)
            .map(ErrorNode.class::cast)
            .findFirst()
            .ifPresent(error -> {
                final String msg = String.format("Could not parse %s '%s'! Error: %s", element, ctx.getText(), error);
                throw new InternalParseException(msg);
            });
    }

    default <T extends AstNode, L extends AstNodeList<T>> List<T> getNodes(final Optional<L> node) {
        return node.map(AstNodeList::nodes).orElse(Collections.emptyList());
    }
}
