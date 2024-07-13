// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.binary;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.TryFromString;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.Operator;
import de.uni_passau.fim.se2.sa.ggnn.ast.parser.InternalParseException;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public enum BinaryOperator implements Operator<BinaryOperator>, Visitable {

    ADD,
    SUBTRACT,
    MULTIPLICATION,
    DIVISION,
    MODULO,
    SHIFT_LEFT,
    SHIFT_RIGHT,
    UNSIGNED_SHIFT_RIGHT,

    AND,
    OR,
    XOR,
    BIGGER_THAN,
    BIGGER_THAN_OR_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    EQUAL,
    NOT_EQUAL,

    BITWISE_AND,
    BITWISE_OR;

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    public boolean isBooleanOperator() {
        return switch (this) {
            case AND, OR, XOR, BIGGER_THAN, BIGGER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, EQUAL, NOT_EQUAL ->
                true;
            case ADD, SUBTRACT, MULTIPLICATION, DIVISION, SHIFT_LEFT, SHIFT_RIGHT, UNSIGNED_SHIFT_RIGHT, MODULO,
                BITWISE_AND, BITWISE_OR -> false;
        };
    }

    public boolean isMathOperator() {
        return !isBooleanOperator();
    }

    public boolean isComparativeOperator() {
        return switch (this) {
            case BIGGER_THAN, BIGGER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, EQUAL, NOT_EQUAL -> true;
            default -> false;
        };
    }

    /**
     * Tries to parse the given string into an operator.
     *
     * @param fromStr The string that should be parsed.
     * @return The parsed operator.
     * @throws InternalParseException Thrown if the string does not represent any known operator.
     */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public static BinaryOperator tryFromString(final String fromStr) throws InternalParseException {
        return switch (fromStr) {
            case "+" -> ADD;
            case "-" -> SUBTRACT;
            case "*" -> MULTIPLICATION;
            case "/" -> DIVISION;
            case "%" -> MODULO;
            case "<<" -> SHIFT_LEFT;
            case ">>" -> SHIFT_RIGHT;
            case ">>>" -> UNSIGNED_SHIFT_RIGHT;
            case "&&" -> AND;
            case "||" -> OR;
            case "^" -> XOR;
            case ">" -> BIGGER_THAN;
            case ">=" -> BIGGER_THAN_OR_EQUAL;
            case "<" -> LESS_THAN;
            case "<=" -> LESS_THAN_OR_EQUAL;
            case "==" -> EQUAL;
            case "!=" -> NOT_EQUAL;
            case "&" -> BITWISE_AND;
            case "|" -> BITWISE_OR;
            default -> throw TryFromString.fromStringFailure(fromStr, BinaryOperator.class);
        };
    }

    /**
     * Returns the complement of a binary operator. If no complement exists, an empty optional is returned.
     *
     * @return The complement of this binary operator wrapped in an optional.
     */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public Optional<BinaryOperator> complement() {
        final BinaryOperator complement = switch (this) {
            case BIGGER_THAN -> LESS_THAN_OR_EQUAL;
            case BIGGER_THAN_OR_EQUAL -> LESS_THAN;
            case LESS_THAN -> BIGGER_THAN_OR_EQUAL;
            case LESS_THAN_OR_EQUAL -> BIGGER_THAN;
            case EQUAL -> NOT_EQUAL;
            case NOT_EQUAL -> EQUAL;
            case AND -> OR;
            case OR -> AND;
            case BITWISE_AND -> BITWISE_OR;
            case BITWISE_OR -> BITWISE_AND;
            default -> null;
        };
        return Optional.ofNullable(complement);
    }

    @Override
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public String toString() {
        return switch (this) {
            case ADD -> "+";
            case SUBTRACT -> "-";
            case MULTIPLICATION -> "*";
            case DIVISION -> "/";
            case MODULO -> "%";
            case SHIFT_LEFT -> "<<";
            case SHIFT_RIGHT -> ">>";
            case UNSIGNED_SHIFT_RIGHT -> ">>>";
            case AND -> "&&";
            case OR -> "||";
            case XOR -> "^";
            case BIGGER_THAN -> ">";
            case BIGGER_THAN_OR_EQUAL -> ">=";
            case LESS_THAN -> "<";
            case LESS_THAN_OR_EQUAL -> "<=";
            case EQUAL -> "==";
            case NOT_EQUAL -> "!=";
            case BITWISE_AND -> "&";
            case BITWISE_OR -> "|";
        };
    }

    @Override
    public List<AstNode> children() {
        return Collections.emptyList();
    }
}
