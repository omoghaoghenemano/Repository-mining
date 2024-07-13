package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.MethodInvocation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.ScopedExpression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.creation.ConstructorInvocation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;

import java.util.Set;

/**
 * Visitor for inferring tokens of variables.
 */
public class VariableTokenVisitor
        implements AstVisitorWithDefaults<Set<SimpleIdentifier>, Set<SimpleIdentifier>> {

    @Override
    public Set<SimpleIdentifier> defaultAction(AstNode node, Set<SimpleIdentifier> arg) {
        node.children().forEach(n -> n.accept(this, arg));
        return arg;
    }

    @Override
    public Set<SimpleIdentifier> visit(MethodInvocation node, Set<SimpleIdentifier> arg) {
        node.arguments().accept(this, arg);
        return arg;
    }

    @Override
    public Set<SimpleIdentifier> visit(ConstructorInvocation node, Set<SimpleIdentifier> arg) {
        node.arguments().accept(this, arg);
        return arg;
    }

    @Override
    public Set<SimpleIdentifier> visit(ScopedExpression node, Set<SimpleIdentifier> arg) {
        node.expr().accept(this, arg);
        return arg;
    }

    @Override
    public Set<SimpleIdentifier> visit(SimpleIdentifier node, Set<SimpleIdentifier> arg) {
        arg.add(node);
        node.children().forEach(n -> n.accept(this, arg));
        return arg;
    }
}
