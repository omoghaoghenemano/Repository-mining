// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.annotation;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNodeList;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.List;

public record ElementValuePairs(List<NamedElementValue> values)
    implements AnnotationValue, AstNodeList<NamedElementValue>, Visitable {

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public List<NamedElementValue> nodes() {
        return List.copyOf(values());
    }
}
