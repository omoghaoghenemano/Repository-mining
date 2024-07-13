// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs.cfg;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.creation.ConstructorInvocation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.QualifiedName;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.ThrowStmt;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement.CatchClause;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.ClassType;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.JoinedTypes;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.QualifiedGenericType;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class UsedExceptionsExtractor {

    private UsedExceptionsExtractor() {
        throw new IllegalCallerException("utility class");
    }

    static Set<String> caughtExceptions(final CatchClause catchClause) {
        final UsedExceptionsVisitor v = new UsedExceptionsVisitor();
        return catchClause.accept(v, null).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Tries to extract the type of the exception that is thrown by the given {@code throw} statement.
     * <p>
     * Note: This only works if the exception is constructed directly as part of this statement, e.g.
     * {@code throws new IOException("msg");}.
     *
     * @param throwStmt The statement from which the exception type is extracted.
     * @return The exception type if it could be extracted.
     */
    static Optional<String> thrownException(final ThrowStmt throwStmt) {
        final UsedExceptionsVisitor v = new UsedExceptionsVisitor();
        return throwStmt.accept(v, null).findAny();
    }

    private static class UsedExceptionsVisitor implements AstVisitorWithDefaults<Stream<String>, Void> {

        @Override
        public Stream<String> defaultAction(AstNode node, Void arg) {
            return node.children().stream().flatMap(c -> c.accept(this, arg));
        }

        @Override
        public Stream<String> visit(ThrowStmt node, Void arg) {
            if (node.expression() instanceof ConstructorInvocation constructorInvocation) {
                return constructorInvocation.className().accept(this, null);
            }
            else {
                return Stream.empty();
            }
        }

        @Override
        public Stream<String> visit(CatchClause node, Void arg) {
            return node.catchType().accept(this, arg);
        }

        @Override
        public Stream<String> visit(JoinedTypes node, Void arg) {
            return node.types().stream().flatMap(t -> t.accept(this, arg));
        }

        @Override
        public Stream<String> visit(QualifiedGenericType node, Void arg) {
            final String identifier = node.identifiers()
                .stream()
                .map(id -> id.identifier().name())
                .collect(Collectors.joining("."));
            return Stream.of(identifier);
        }

        @Override
        public Stream<String> visit(ClassType node, Void arg) {
            return node.scope()
                .map(scope -> scope.accept(this, arg))
                .orElse(Stream.of(""))
                .map(scope -> scope + "." + node.identifier().name());
        }

        @Override
        public Stream<String> visit(SimpleIdentifier node, Void arg) {
            return Stream.of(node.name());
        }

        @Override
        public Stream<String> visit(QualifiedName node, Void arg) {
            final String name = node.identifiers().stream().map(SimpleIdentifier::name)
                .collect(Collectors.joining("."));
            return Stream.of(name);
        }
    }
}
