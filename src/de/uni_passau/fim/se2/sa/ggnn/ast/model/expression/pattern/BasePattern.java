// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.pattern;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.VariableModifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.annotation.Annotation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.Type;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.ArrayList;
import java.util.List;

public record BasePattern(
    List<VariableModifier> modifiers,
    Type type,
    List<Annotation> annotations,
    SimpleIdentifier identifier
) implements Pattern, Visitable {

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public List<AstNode> children() {
        final var children = new ArrayList<AstNode>(modifiers());
        children.add(type());
        children.addAll(annotations());
        children.add(identifier());
        return children;
    }
}
