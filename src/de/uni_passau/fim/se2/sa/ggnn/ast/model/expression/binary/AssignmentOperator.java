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

public enum AssignmentOperator implements Operator<AssignmentOperator>, TryFromString<AssignmentOperator>, Visitable {

    ASSIGN,
    ADD_ASSIGN,
    SUBTRACT_ASSIGN,
    MULTIPLICATION_ASSIGN,
    DIVISION_ASSIGN,
    AND_ASSIGN,
    OR_ASSIGN,
    XOR_ASSIGN,
    SHIFT_LEFT_ASSIGN,
    SHIFT_RIGHT_ASSIGN,
    UNSIGNED_SHIFT_RIGHT_ASSIGN,
    MODULO_ASSIGN;

    /**
     * Tries to parse the given string into an operator.
     *
     * @param s The string that should be parsed.
     * @return The parsed operator.
     * @throws InternalParseException Thrown if the string does not represent any known operator.
     */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public static AssignmentOperator tryFromString(String s) throws InternalParseException {
        return switch (s) {
            case "=" -> ASSIGN;
            case "+=" -> ADD_ASSIGN;
            case "-=" -> SUBTRACT_ASSIGN;
            case "*=" -> MULTIPLICATION_ASSIGN;
            case "/=" -> DIVISION_ASSIGN;
            case "&=" -> AND_ASSIGN;
            case "|=" -> OR_ASSIGN;
            case "^=" -> XOR_ASSIGN;
            case ">>=" -> SHIFT_RIGHT_ASSIGN;
            case ">>>=" -> UNSIGNED_SHIFT_RIGHT_ASSIGN;
            case "<<=" -> SHIFT_LEFT_ASSIGN;
            case "%=" -> MODULO_ASSIGN;
            default -> throw TryFromString.fromStringFailure(s, AssignmentOperator.class);
        };
    }

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public String toString() {
        return switch (this) {
            case ASSIGN -> "=";
            case ADD_ASSIGN -> "+=";
            case SUBTRACT_ASSIGN -> "-=";
            case MULTIPLICATION_ASSIGN -> "*=";
            case DIVISION_ASSIGN -> "/=";
            case AND_ASSIGN -> "&=";
            case OR_ASSIGN -> "|=";
            case XOR_ASSIGN -> "^=";
            case SHIFT_LEFT_ASSIGN -> "<<=";
            case SHIFT_RIGHT_ASSIGN -> ">>=";
            case UNSIGNED_SHIFT_RIGHT_ASSIGN -> ">>>=";
            case MODULO_ASSIGN -> "%=";
        };
    }

    @Override
    public List<AstNode> children() {
        return Collections.emptyList();
    }
}
