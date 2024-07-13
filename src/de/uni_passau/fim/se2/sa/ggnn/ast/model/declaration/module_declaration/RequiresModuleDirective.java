// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.module_declaration;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record RequiresModuleDirective(List<RequiresModifier> modifiers, Identifier required)
    implements ModuleDirective, Visitable {

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public List<AstNode> children() {
        final var children = new ArrayList<AstNode>(modifiers());
        children.add(required());
        return children;
    }

    public enum RequiresModifier implements AstNode, Visitable {

        TRANSITIVE,
        STATIC,
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
