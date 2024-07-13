// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier;

import com.google.common.base.Preconditions;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;

import java.util.List;

public interface VariableDeclarationIdentifier extends Identifier {

    SimpleIdentifier identifier();

    record ArrayVariableIdentifier(SimpleIdentifier identifier, int arrayDimensions)
        implements VariableDeclarationIdentifier {

        public ArrayVariableIdentifier {
            Preconditions.checkArgument(arrayDimensions > 0, "An array variable needs to have dimension > 0.");
        }

        @Override
        public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
            return visitor.visit(this, arg);
        }

        @Override
        public List<AstNode> children() {
            return List.of(identifier());
        }
    }
}
