// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.util.dotgraph;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.Modifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.binary.AssignmentOperator;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.binary.BinaryOperator;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.literal.LiteralValueExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.unary.UnaryOperator;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.QualifiedName;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.BreakStmt;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.ContinueStmt;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.LabelledStmt;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.SwitchCase;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.labels.DefaultLabel;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.labels.SimpleLabel;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.PrimitiveType;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;

/**
 * Determines the node label in the output graph.
 */
public final class AstNodeLabelGenerator {

    private AstNodeLabelGenerator() {
    }

    public static String getLabel(final AstNode node) {
        final AstNodeLabelVisitor labelVisitor = new AstNodeLabelVisitor();
        return node.accept(labelVisitor, null);
    }

    private static class AstNodeLabelVisitor implements AstVisitorWithDefaults<String, Void> {

        @Override
        public String defaultAction(AstNode node, Void arg) {
            return node.getClass().getSimpleName();
        }

        @Override
        public <V> String visit(LiteralValueExpr<V> node, Void arg) {
            return node.value().toString();
        }

        @Override
        public String visit(Modifier node, Void arg) {
            return node.toString();
        }

        @Override
        public String visit(PrimitiveType node, Void arg) {
            return node.toString();
        }

        @Override
        public String visit(SimpleIdentifier node, Void arg) {
            return node.name();
        }

        @Override
        public String visit(AssignmentOperator node, Void arg) {
            return node.toString();
        }

        @Override
        public String visit(BinaryOperator node, Void arg) {
            return node.toString();
        }

        @Override
        public String visit(UnaryOperator node, Void arg) {
            return node.toString();
        }

        @Override
        public String visit(QualifiedName node, Void arg) {
            return node.toString();
        }

        @Override
        public String visit(BreakStmt node, Void arg) {
            return "break" + node.identifier().map(id -> " " + id).orElse("");
        }

        @Override
        public String visit(ContinueStmt node, Void arg) {
            return "continue" + node.identifier().map(id -> " " + id).orElse("");
        }

        @Override
        public String visit(LabelledStmt node, Void arg) {
            return node.label().toString() + ": " + node.statement().accept(this, arg);
        }

        @Override
        public String visit(SwitchCase node, Void arg) {
            if (node.label() instanceof DefaultLabel) {
                return "default";
            }
            else {
                return "case " + node.label().accept(this, arg);
            }
        }

        @Override
        public String visit(SimpleLabel node, Void arg) {
            return node.label().toString();
        }
    }
}
