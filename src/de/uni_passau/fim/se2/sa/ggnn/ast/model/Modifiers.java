// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.annotation.Annotation;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public record Modifiers(List<Annotation> annotations, List<Modifier> basicModifiers)
    implements AstNode, Visitable {

    private static final Modifiers EMPTY = new Modifiers(Collections.emptyList(), Collections.emptyList());

    public static Modifiers empty() {
        return EMPTY;
    }

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    public Modifiers merge(Modifiers other) {
        return new Modifiers(merged(annotations, other.annotations), merged(basicModifiers, other.basicModifiers));
    }

    public static Modifiers merge(Stream<Modifiers> modifiers) {
        return modifiers.reduce(EMPTY, Modifiers::merge);
    }

    private static <T> List<T> merged(List<T> a, List<T> b) {
        return Stream.concat(a.stream(), b.stream()).toList();
    }

    @Override
    public List<AstNode> children() {
        final var children = new ArrayList<AstNode>(annotations());
        children.addAll(basicModifiers());
        return children;
    }
}
