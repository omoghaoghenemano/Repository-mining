// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.record_declaration;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNodeList;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.List;

public record RecordComponents(List<RecordComponent> components)
    implements AstNodeList<RecordComponent>, Visitable {

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public List<RecordComponent> nodes() {
        return List.copyOf(components());
    }
}
