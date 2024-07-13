// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.unary;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.TryFromString;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.Operator;
import de.uni_passau.fim.se2.sa.ggnn.ast.parser.InternalParseException;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.Collections;
import java.util.List;

public enum UnaryOperator implements Operator<UnaryOperator>, Visitable {

    INCREMENT,
    DECREMENT,
    POSITIVE,
    NEGATIVE,
    BINARY_COMPLEMENT,
    NOT;

    /**
     * Tries to parse the given string into an operator.
     *
     * @param s The string that should be parsed.
     * @return The parsed operator.
     * @throws InternalParseException Thrown if the string does not represent any known operator.
     */
    public static UnaryOperator tryFromString(String s) throws InternalParseException {
        return switch (s) {
            case "++" -> INCREMENT;
            case "--" -> DECREMENT;
            case "+" -> POSITIVE;
            case "-" -> NEGATIVE;
            case "~" -> BINARY_COMPLEMENT;
            case "!" -> NOT;
            default -> throw TryFromString.fromStringFailure(s, UnaryOperator.class);
        };
    }

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public String toString() {
        return switch (this) {
            case INCREMENT -> "++";
            case DECREMENT -> "--";
            case POSITIVE -> "+";
            case NEGATIVE -> "-";
            case BINARY_COMPLEMENT -> "~";
            case NOT -> "!";
        };
    }

    @Override
    public List<AstNode> children() {
        return Collections.emptyList();
    }
}
