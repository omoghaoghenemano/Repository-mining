// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model;

import de.uni_passau.fim.se2.sa.ggnn.ast.parser.InternalParseException;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public enum Modifier implements AstNode, Visitable {

    PUBLIC,
    PROTECTED,
    PRIVATE,
    STATIC,
    ABSTRACT,
    FINAL,
    STRICTFP,
    SEALED,
    NON_SEALED,
    NATIVE,
    SYNCHRONIZED,
    TRANSIENT,
    VOLATILE,
    DEFAULT,
    ;

    /**
     * Tries to parse the given string into a modifier.
     *
     * @param s The string that should be parsed.
     * @return The parsed modifier.
     * @throws InternalParseException Thrown if the string does not represent any known modifier.
     */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public static Modifier tryFromString(String s) {
        return switch (s) {
            case "public" -> PUBLIC;
            case "protected" -> PROTECTED;
            case "private" -> PRIVATE;
            case "static" -> STATIC;
            case "abstract" -> ABSTRACT;
            case "final" -> FINAL;
            case "strictfp" -> STRICTFP;
            case "sealed" -> SEALED;
            case "non-sealed" -> NON_SEALED;
            case "native" -> NATIVE;
            case "synchronized" -> SYNCHRONIZED;
            case "transient" -> TRANSIENT;
            case "volatile" -> VOLATILE;
            case "default" -> DEFAULT;
            default -> throw TryFromString.fromStringFailure(s, Modifier.class);
        };
    }

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT).replace("_", "-");
    }

    @Override
    public List<AstNode> children() {
        return Collections.emptyList();
    }
}
