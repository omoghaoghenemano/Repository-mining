// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.type;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.annotation.Annotation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record TypeParameter(Optional<Annotation> annotation, SimpleIdentifier identifier, Optional<Bound> bound)
    implements AstNode, Visitable {

    public TypeParameter(SimpleIdentifier identifier) {
        this(Optional.empty(), identifier, Optional.empty());
    }

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public List<AstNode> children() {
        final var children = new ArrayList<AstNode>();
        annotation().ifPresent(children::add);
        children.add(identifier());
        bound().ifPresent(children::add);
        return children;
    }

    public record Bound(Optional<Annotation> annotation, List<Type> bounds) implements AstNode, Visitable {

        @Override
        public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
            return visitor.visit(this, arg);
        }

        @Override
        public List<AstNode> children() {
            final var children = new ArrayList<AstNode>();
            annotation().ifPresent(children::add);
            children.addAll(bounds());
            return children;
        }
    }
}