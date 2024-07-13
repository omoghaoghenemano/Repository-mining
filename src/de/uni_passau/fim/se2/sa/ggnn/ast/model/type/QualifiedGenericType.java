// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.type;

import com.google.common.base.Preconditions;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.MaybeGenericIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;

import java.util.List;

public record QualifiedGenericType(List<MaybeGenericIdentifier> identifiers) implements ClassOrInterfaceType {

    public QualifiedGenericType {
        Preconditions.checkArgument(identifiers.size() >= 2, "A qualified type needs to have at least two name parts.");
    }

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public List<AstNode> children() {
        return List.copyOf(identifiers());
    }
}
