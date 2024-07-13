// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.Block;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.Statement;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface TryStmt extends Statement {

    List<CatchClause> catchClauses();

    Optional<Block> finallyBlock();

    Block block();

    record TryWithResources(
        Resources resourceSpecStmt, Block block, List<CatchClause> catchClauses, Optional<Block> finallyBlock
    ) implements TryStmt, Visitable {

        @Override
        public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
            return visitor.visit(this, arg);
        }

        @Override
        public List<AstNode> children() {
            final var children = new ArrayList<AstNode>();
            children.add(resourceSpecStmt());
            children.add(block());
            children.addAll(catchClauses());
            finallyBlock().ifPresent(children::add);
            return children;
        }
    }

    record RegularTry(Block block, List<CatchClause> catchClauses, Optional<Block> finallyBlock)
        implements TryStmt, Visitable {

        @Override
        public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
            return visitor.visit(this, arg);
        }

        @Override
        public List<AstNode> children() {
            final var children = new ArrayList<AstNode>();
            children.add(block());
            children.addAll(catchClauses());
            finallyBlock().ifPresent(children::add);
            return children;
        }
    }
}
