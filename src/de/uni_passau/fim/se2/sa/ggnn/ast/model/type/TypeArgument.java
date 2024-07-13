// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.type;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.annotation.Annotation;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface TypeArgument extends AstNode {

    record WildcardType(
        List<Annotation> annotations, Optional<Pair<WildcardTypeRestriction, Type>> typeRestriction
    ) implements TypeArgument, Visitable {

        @Override
        public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
            return visitor.visit(this, arg);
        }

        @Override
        public List<AstNode> children() {
            final var children = new ArrayList<AstNode>(annotations());
            typeRestriction().ifPresent(t -> {
                children.add(t.a());
                children.add(t.b());
            });
            return children;
        }
    }

    enum WildcardTypeRestriction implements AstNode, Visitable {

        EXTENDS,
        SUPER,
        ;

        @Override
        public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
            return visitor.visit(this, arg);
        }

        @Override
        public List<AstNode> children() {
            return Collections.emptyList();
        }
    }
}
