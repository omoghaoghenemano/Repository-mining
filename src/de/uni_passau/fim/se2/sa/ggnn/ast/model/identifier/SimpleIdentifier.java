// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.ClassOrInterfaceType;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.Collections;
import java.util.List;

public record SimpleIdentifier(String name)
    implements Identifier, VariableDeclarationIdentifier, Visitable, ClassOrInterfaceType, MaybeGenericIdentifier {

    private static final SimpleIdentifier NEW_ID = new SimpleIdentifier("new");
    private static final SimpleIdentifier SUPER_ID = new SimpleIdentifier("super");
    private static final SimpleIdentifier THIS_ID = new SimpleIdentifier("this");

    public static SimpleIdentifier newIdentifier() {
        return NEW_ID;
    }

    public static SimpleIdentifier superIdentifier() {
        return SUPER_ID;
    }

    public static SimpleIdentifier thisIdentifier() {
        return THIS_ID;
    }

    @Override
    public SimpleIdentifier identifier() {
        return this;
    }

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public List<AstNode> children() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return name();
    }
}
