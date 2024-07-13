// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.statement;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.VariableModifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.VariableDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.Expression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.Type;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.ArrayList;
import java.util.List;

public interface LocalVariableDeclStmt extends Statement {

    List<VariableModifier> modifiers();

    record LocalVarVariableDecl(List<VariableModifier> modifiers, Identifier identifier, Expression initializer)
        implements LocalVariableDeclStmt, Visitable {

        @Override
        public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
            return visitor.visit(this, arg);
        }

        @Override
        public List<AstNode> children() {
            final var children = new ArrayList<AstNode>(modifiers());
            children.addAll(List.of(identifier(), initializer()));
            return children;
        }
    }

    record LocalTypedVariableDecl(List<VariableModifier> modifiers, Type type, List<VariableDeclaration> declarations)
        implements LocalVariableDeclStmt, Visitable {

        @Override
        public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
            return visitor.visit(this, arg);
        }

        @Override
        public List<AstNode> children() {
            final var children = new ArrayList<AstNode>(modifiers());
            children.add(type());
            children.addAll(declarations());
            return children;
        }
    }
}
