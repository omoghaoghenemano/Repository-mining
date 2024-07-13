// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.labels;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.List;

/**
 * Used when multiple cases without any statements follow after each other.
 * <p>
 * E.g., for
 *
 * <pre>
 * {@code
 * switch (x) {
 *     case 1:
 *     case 2:
 *     case 3:
 *         foo();
 * }
 * }
 * </pre>
 *
 * a {@code MultipleLabel} with three labels is created.
 * <p>
 * For
 *
 * <pre>
 * {@code
 * switch (x) {
 *     case 1, 2, 3:
 *         foo();
 * }
 * }
 * </pre>
 *
 * uses a {@link ChoiceLabel} instead.
 *
 * @param labels The labels this label is composed of.
 */
public record MultipleLabel(List<SwitchLabel> labels) implements SwitchLabel, Visitable {

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public List<AstNode> children() {
        return List.copyOf(labels());
    }
}
