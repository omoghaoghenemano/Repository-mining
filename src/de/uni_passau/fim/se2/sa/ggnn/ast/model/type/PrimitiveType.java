// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.type;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.TryFromString;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.parser.InternalParseException;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public enum PrimitiveType implements Type, TryFromString<PrimitiveType>, Visitable {

    BOOLEAN,
    CHAR,
    BYTE,
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE;

    /**
     * Tries to parse the given string into a type.
     *
     * @param fromStr The string that should be parsed.
     * @return The parsed type.
     * @throws InternalParseException Thrown if the string does not represent any known type.
     */
    public static PrimitiveType tryFromString(String fromStr) throws InternalParseException {
        return switch (fromStr) {
            case "boolean" -> BOOLEAN;
            case "char" -> CHAR;
            case "byte" -> BYTE;
            case "short" -> SHORT;
            case "int" -> INT;
            case "long" -> LONG;
            case "float" -> FLOAT;
            case "double" -> DOUBLE;
            default -> throw TryFromString.fromStringFailure(fromStr, PrimitiveType.class);
        };
    }

    /**
     * Gets the wrapper class of a {@link PrimitiveType}.
     *
     * @return The wrapper class.
     */
    public ClassOrInterfaceType getWrapperType() {
        final Class<?> wrapperClass = switch (this) {
            case INT -> Integer.class;
            case BOOLEAN -> Boolean.class;
            case CHAR -> Character.class;
            case BYTE -> Byte.class;
            case SHORT -> Short.class;
            case LONG -> Long.class;
            case FLOAT -> Float.class;
            case DOUBLE -> Double.class;
        };
        final String simpleName = wrapperClass.getSimpleName();
        return new SimpleIdentifier(simpleName);
    }

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public List<AstNode> children() {
        return Collections.emptyList();
    }
}
