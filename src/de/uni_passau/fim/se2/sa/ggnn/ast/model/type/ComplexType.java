// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.type;

import com.google.common.base.Preconditions;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.annotation.Annotation;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.ArrayList;
import java.util.List;

public record ComplexType(List<Annotation> annotations, Type baseType, List<ArrayType> arrayTypes)
    implements AstNode, Visitable, Type {

    public ComplexType {
        final boolean emptyAnnotations = annotations == null || annotations.isEmpty();
        final boolean emptyArray = arrayTypes == null || arrayTypes.isEmpty();
        Preconditions.checkArgument(
            !(emptyAnnotations && emptyArray),
            "A complex type has to either have annotations or be an array type, otherwise it is a simple type."
        );
    }

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public List<AstNode> children() {
        final var children = new ArrayList<AstNode>(annotations());
        children.add(baseType());
        children.addAll(arrayTypes());
        return children;
    }

    public record ArrayType(List<Annotation> annotations) implements AstNode, Visitable {

        @Override
        public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
            return visitor.visit(this, arg);
        }

        @Override
        public List<AstNode> children() {
            return List.copyOf(annotations());
        }
    }
}
