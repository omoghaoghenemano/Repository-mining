// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.parser;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.Arguments;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.annotation.Annotation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.Body;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.ClassBodyDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.binary.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.creation.AnonymousClassCreation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.creation.ArrayCreation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.creation.ClassCreationExpression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.creation.ConstructorInvocation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.lambda.LambdaExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.lambda.LambdaParameters;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.literal.LiteralValueExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.unary.UnaryExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.unary.UnaryOperator;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.MaybeGenericIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.Block;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.Switch;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.SwitchCase;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.JoinedTypes;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.Type;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.javaparser.JavaParser;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class AntlrExpressionAstConversionVisitor extends AntlrAstBaseConversionVisitor implements AntlrAstConverter {

    private final AntlrAstConversionVisitor conversionVisitor;

    AntlrExpressionAstConversionVisitor(final AntlrAstConversionVisitor conversionVisitor) {
        this.conversionVisitor = conversionVisitor;
    }

    @Override
    @SuppressWarnings({ "checkstyle:cyclomaticComplexity", "PMD.CognitiveComplexity" })
    public Expression visitExpression(JavaParser.ExpressionContext ctx) {
        checkErrorNode("expression", ctx);

        if (ctx.primary() != null) {
            return visitPrimary(ctx.primary());
        }
        else if (ctx.scopeMarker != null) {
            return visitScopedExpression(ctx);
        }
        else if (ctx.methodCall() != null) {
            return visitMethodCall(ctx.methodCall());
        }
        else if (ctx.NEW() != null && ctx.creator() != null) {
            return visitCreator(ctx.creator());
        }
        else if (ctx.postfix != null || ctx.prefix != null) {
            return visitUnaryExpression(ctx);
        }
        else if (ctx.bop != null) {
            return visitBinaryExpression(ctx);
        }
        else if (ctx.tern != null) {
            return visitTernaryExpression(ctx);
        }
        else if (ctx.INSTANCEOF() != null) {
            return visitInstanceOfExpression(ctx);
        }
        else if (ctx.LBRACK() != null && ctx.RBRACK() != null) {
            return visitArrayAccessExpression(ctx);
        }
        else if (ctx.assignOp != null) {
            return visitAssignmentExpression(ctx);
        }
        else if (ctx.COLONCOLON() != null) {
            return visitMethodReference(ctx);
        }
        else if (ctx.toCast != null) {
            return visitCastExpression(ctx);
        }
        else if (ctx.lambdaExpression() != null) {
            return visitLambdaExpression(ctx.lambdaExpression());
        }
        else if (ctx.switchExpression() != null) {
            return visitSwitchExpression(ctx.switchExpression());
        }
        else if (hasShiftOperator(ctx)) {
            return visitShiftExpression(ctx);
        }

        throw new InternalParseException("Unknown expression.", ctx.getText());
    }

    @Override
    public Expression visitPrimary(JavaParser.PrimaryContext ctx) {
        checkErrorNode("expression", ctx);

        if (ctx.parExpr != null) {
            return parExpression(ctx.parExpr);
        }
        else if (ctx.literal() != null) {
            return conversionVisitor.visitLiteral(ctx.literal());
        }
        else if (ctx.identifier() != null) {
            return conversionVisitor.visitIdentifier(ctx.identifier());
        }
        else if (ctx.THIS() != null && ctx.arguments() == null) {
            return SimpleIdentifier.thisIdentifier();
        }
        else if (ctx.SUPER() != null) {
            return SimpleIdentifier.superIdentifier();
        }
        else if (ctx.CLASS() != null) {
            final Type classType = conversionVisitor.visitTypeTypeOrVoid(ctx.typeTypeOrVoid());
            return new ClassReference(classType);
        }
        else if (ctx.nonWildcardTypeArguments() != null) {
            return primaryGenericInvocation(ctx);
        }

        throw new InternalParseException("Unknown expression.", ctx.getText());
    }

    private ParExpression parExpression(final JavaParser.ExpressionContext ctx) {
        final Expression expr = visitExpression(ctx);
        return new ParExpression(expr);
    }

    private ExplicitGenericInvocation primaryGenericInvocation(final JavaParser.PrimaryContext ctx) {
        final InvocationExpression invocation;
        if (ctx.explicitGenericInvocationSuffix() != null) {
            invocation = visitExplicitGenericInvocationSuffix(ctx.explicitGenericInvocationSuffix());
        }
        else if (ctx.THIS() != null) {
            final Arguments arguments = conversionVisitor.visitArguments(ctx.arguments());
            invocation = new ThisInvocation(arguments);
        }
        else {
            throw new InternalParseException("Unknown generic method invocation.", ctx.getText());
        }

        final TypeArguments typeArguments = conversionVisitor
            .visitNonWildcardTypeArguments(ctx.nonWildcardTypeArguments());

        return new ExplicitGenericInvocation(typeArguments.arguments(), invocation);
    }

    @Override
    public InvocationExpression visitMethodCall(JavaParser.MethodCallContext ctx) {
        checkErrorNode("method invocation", ctx);

        final Arguments arguments = new Arguments(
            getNodes(visitNullable(ctx.expressionList(), conversionVisitor::visitExpressionList))
        );

        if (ctx.THIS() != null) {
            return new ThisInvocation(arguments);
        }
        else if (ctx.SUPER() != null) {
            return new SuperInvocation.SuperConstructorInvocation(arguments);
        }
        else {
            final SimpleIdentifier identifier = conversionVisitor.visitIdentifier(ctx.identifier());
            return new MethodInvocation(identifier, arguments);
        }
    }

    @Override
    public LambdaExpr visitLambdaExpression(JavaParser.LambdaExpressionContext ctx) {
        checkErrorNode("lambda initializer", ctx);

        final LambdaParameters parameters = visitLambdaParameters(ctx.lambdaParameters());
        final AstNode body = visitLambdaBody(ctx.lambdaBody());

        if (body instanceof Expression e) {
            return new LambdaExpr.ExprLambda(parameters, e);
        }
        else if (body instanceof Block b) {
            return new LambdaExpr.BlockLambda(parameters, b);
        }
        else {
            throw new InternalParseException("Unknown lambda body type: ", ctx.lambdaBody().getText());
        }
    }

    @Override
    public LambdaParameters visitLambdaParameters(JavaParser.LambdaParametersContext ctx) {
        checkErrorNode("lambda parameters", ctx);

        if (ctx.lambdaLVTIList() != null) {
            return conversionVisitor.visitLambdaLVTIList(ctx.lambdaLVTIList());
        }
        else if (ctx.formalParameterList() != null) {
            return conversionVisitor.visitFormalParameterList(ctx.formalParameterList());
        }
        else if (ctx.identifier() != null) {
            final List<SimpleIdentifier> parameters = visitNullableList(
                ctx.identifier(), conversionVisitor::visitIdentifier
            );
            return new LambdaParameters.IdentifierParameters(parameters);
        }
        else {
            throw new InternalParseException("Unknown lambda parameters type: ", ctx.getText());
        }
    }

    @Override
    public AstNode visitLambdaBody(JavaParser.LambdaBodyContext ctx) {
        checkErrorNode("lambda body", ctx);

        if (ctx.expression() != null) {
            return visitExpression(ctx.expression());
        }
        else if (ctx.block() != null) {
            return conversionVisitor.visitBlock(ctx.block());
        }
        else {
            throw new InternalParseException("Unknown lambda type (neither initializer nor block): ", ctx.getText());
        }
    }

    @Override
    public Switch.SwitchExpr visitSwitchExpression(JavaParser.SwitchExpressionContext ctx) {
        checkErrorNode("switch initializer", ctx);

        final Expression check = conversionVisitor.visitParExpression(ctx.parExpression());
        final List<SwitchCase> cases = visitNullableList(
            ctx.switchLabeledRule(), conversionVisitor::visitSwitchLabeledRule
        );

        return new Switch.SwitchExpr(check, cases);
    }

    @Override
    public SuperInvocation visitSuperSuffix(JavaParser.SuperSuffixContext ctx) {
        checkErrorNode("super invocation", ctx);

        if (ctx.identifier() == null) {
            return new SuperInvocation.SuperConstructorInvocation(conversionVisitor.visitArguments(ctx.arguments()));
        }
        else {
            final Optional<TypeArguments> typeArguments = visitNullable(
                ctx.typeArguments(),
                conversionVisitor::visitTypeArguments
            );
            final SimpleIdentifier name = conversionVisitor.visitIdentifier(ctx.identifier());
            final Arguments arguments = visitNullable(ctx.arguments(), conversionVisitor::visitArguments)
                .orElse(new Arguments(Collections.emptyList()));

            return new SuperInvocation.SuperMethodInvocation(getNodes(typeArguments), name, arguments);
        }
    }

    @Override
    public ExplicitGenericInvocation visitExplicitGenericInvocation(JavaParser.ExplicitGenericInvocationContext ctx) {
        checkErrorNode("explicit generic invocation", ctx);

        final TypeArguments typeArguments = conversionVisitor
            .visitNonWildcardTypeArguments(ctx.nonWildcardTypeArguments());
        final InvocationExpression invocation = visitExplicitGenericInvocationSuffix(
            ctx.explicitGenericInvocationSuffix()
        );

        return new ExplicitGenericInvocation(typeArguments.arguments(), invocation);
    }

    @Override
    public InvocationExpression visitExplicitGenericInvocationSuffix(
        JavaParser.ExplicitGenericInvocationSuffixContext ctx
    ) {
        checkErrorNode("generic invocation suffix", ctx);

        if (ctx.SUPER() == null) {
            final SimpleIdentifier identifier = conversionVisitor.visitIdentifier(ctx.identifier());
            final Arguments arguments = conversionVisitor.visitArguments(ctx.arguments());
            return new MethodInvocation(identifier, arguments);
        }
        else {
            return visitSuperSuffix(ctx.superSuffix());
        }
    }

    @Override
    public ClassCreationExpression visitCreator(JavaParser.CreatorContext ctx) {
        checkErrorNode("creator", ctx);

        final Type createdName = visitCreatedName(ctx.createdName());

        if (ctx.classCreatorRest() == null) {
            final ArrayCreation arr = visitArrayCreatorRest(ctx.arrayCreatorRest());
            return new ArrayCreation(
                Collections.emptyList(), createdName, arr.dimension(), arr.knownSizes(), arr.initializer()
            );
        }

        final Optional<TypeArguments> typeArguments = visitNullable(
            ctx.nonWildcardTypeArguments(),
            conversionVisitor::visitNonWildcardTypeArguments
        );
        final ClassCreatorRest rest = visitClassCreatorRest(ctx.classCreatorRest());
        if (rest.body.isPresent()) {
            final Body<ClassBodyDeclaration> body = rest.body.get();
            return new AnonymousClassCreation(
                getNodes(typeArguments), createdName, Collections.emptyList(), rest.arguments(), body
            );
        }
        else {
            return new ConstructorInvocation(
                Collections.emptyList(), createdName, Collections.emptyList(), rest.arguments()
            );
        }
    }

    @Override
    public Type visitCreatedName(JavaParser.CreatedNameContext ctx) {
        checkErrorNode("created name", ctx);

        if (ctx.primitiveType() == null) {
            final List<MaybeGenericIdentifier> identifiers = visitNullableList(
                ctx.createdNameIdentifier(),
                this::visitCreatedNameIdentifier
            );
            return classTypeFromIdentifiers(identifiers);
        }
        else {
            return conversionVisitor.visitPrimitiveType(ctx.primitiveType());
        }
    }

    @Override
    public MaybeGenericIdentifier visitCreatedNameIdentifier(JavaParser.CreatedNameIdentifierContext ctx) {
        checkErrorNode("created name label", ctx);

        final SimpleIdentifier identifier = conversionVisitor.visitIdentifier(ctx.identifier());
        final Optional<TypeArguments> typeArguments = visitNullable(
            ctx.typeArgumentsOrDiamond(),
            conversionVisitor::visitTypeArgumentsOrDiamond
        );
        return buildIdentifier(identifier, typeArguments);
    }

    @Override
    public ClassCreationExpression visitInnerCreator(JavaParser.InnerCreatorContext ctx) {
        checkErrorNode("inner creator", ctx);

        final SimpleIdentifier identifier = conversionVisitor.visitIdentifier(ctx.identifier());
        final Optional<TypeArguments> typeArguments = visitNullable(
            ctx.nonWildcardTypeArgumentsOrDiamond(),
            conversionVisitor::visitNonWildcardTypeArgumentsOrDiamond
        );
        final ClassCreatorRest rest = visitClassCreatorRest(ctx.classCreatorRest());

        if (rest.body.isPresent()) {
            final Body<ClassBodyDeclaration> body = rest.body.get();
            return new AnonymousClassCreation(
                Collections.emptyList(), identifier, getNodes(typeArguments), rest.arguments(), body
            );
        }
        else {
            return new ConstructorInvocation(
                Collections.emptyList(), identifier, getNodes(typeArguments), rest.arguments()
            );
        }
    }

    @Override
    public ArrayCreation visitArrayCreatorRest(JavaParser.ArrayCreatorRestContext ctx) {
        checkErrorNode("array creator", ctx);

        final Optional<Expression> initializer = visitNullable(
            ctx.arrayInitializer(),
            conversionVisitor::visitArrayInitializer
        );
        final int dimension = ctx.LBRACK().size();
        final List<Expression> knownSizes = visitNullableList(ctx.expression(), conversionVisitor::visitExpression);

        return new ArrayCreation(Collections.emptyList(), new Type.VoidType(), dimension, knownSizes, initializer);
    }

    private record ClassCreatorRest(Arguments arguments, Optional<Body<ClassBodyDeclaration>> body)
        implements HelperAstNode {

        @Override
        public List<AstNode> children() {
            final var children = new ArrayList<AstNode>();
            children.add(arguments());
            body().ifPresent(children::add);
            return children;
        }

        @Override
        public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
            return null;
        }
    }

    @Override
    public ClassCreatorRest visitClassCreatorRest(JavaParser.ClassCreatorRestContext ctx) {
        checkErrorNode("class creator rest", ctx);

        final Arguments arguments = conversionVisitor.visitArguments(ctx.arguments());
        final Optional<Body<ClassBodyDeclaration>> body = visitNullable(
            ctx.classBody(), conversionVisitor::visitClassBody
        );

        return new ClassCreatorRest(arguments, body);
    }

    private Expression visitUnaryExpression(final JavaParser.ExpressionContext ctx) {
        // Long.MIN_VALUE without the sign is too big to be stored in a Long and
        // parsing it like a regular number would therefore result in a
        // NumberFormatException.
        if (AntlrAstConversionVisitor.isLongMinValueExpr(ctx.getText())) {
            return new LiteralValueExpr<>(BigInteger.valueOf(Long.MIN_VALUE));
        }
        else {
            return visitRegularUnaryExpression(ctx);
        }
    }

    private Expression visitRegularUnaryExpression(final JavaParser.ExpressionContext ctx) {
        final var operator = getUnaryExprOperator(ctx);
        final var expr = visitExpression(ctx.expression(0));

        if (UnaryOperator.NEGATIVE.equals(operator.b()) && expr instanceof LiteralValueExpr<?> literal) {
            return getSignedLiteralValueExpr(literal);
        }
        else {
            return new UnaryExpr(operator.a(), operator.b(), expr);
        }
    }

    private LiteralValueExpr<?> getSignedLiteralValueExpr(final LiteralValueExpr<?> literal) {
        final Object value = literal.value();
        if (value instanceof BigInteger l) {
            return new LiteralValueExpr<>(l.multiply(BigInteger.valueOf(-1)));
        }
        else if (value instanceof Character c) {
            return new LiteralValueExpr<>(-c);
        }
        else {
            final double d = (Double) value;
            return new LiteralValueExpr<>(-1D * d);
        }
    }

    private Pair<UnaryExpr.UnaryExprType, UnaryOperator> getUnaryExprOperator(final JavaParser.ExpressionContext ctx) {
        final UnaryExpr.UnaryExprType type;
        final UnaryOperator operator;

        if (ctx.prefix != null) {
            type = UnaryExpr.UnaryExprType.PREFIX;
            operator = UnaryOperator.tryFromString(ctx.prefix.getText());
        }
        else if (ctx.postfix != null) {
            type = UnaryExpr.UnaryExprType.POSTFIX;
            operator = UnaryOperator.tryFromString(ctx.postfix.getText());
        }
        else {
            throw new InternalParseException("Unknown unary initializer operator type!");
        }

        return Pair.of(type, operator);
    }

    private ScopedExpression visitScopedExpression(final JavaParser.ExpressionContext ctx) {
        final Expression expr;

        if (ctx.identifier() != null) {
            expr = conversionVisitor.visitIdentifier(ctx.identifier());
        }
        else if (ctx.methodCall() != null) {
            expr = visitMethodCall(ctx.methodCall());
        }
        else if (ctx.THIS() != null) {
            expr = SimpleIdentifier.thisIdentifier();
        }
        else if (ctx.NEW() != null) {
            // instantiation of non-static inner classes -> scoped on a concrete instances of the surrounding class
            final Optional<TypeArguments> typeArgs = visitNullable(
                ctx.nonWildcardTypeArguments(),
                conversionVisitor::visitNonWildcardTypeArguments
            );
            final ClassCreationExpression creator = visitInnerCreator(ctx.innerCreator());
            expr = typeArgs.map(t -> creator.withTypeArguments(t.arguments())).orElse(creator);
        }
        else if (ctx.superSuffix() != null) {
            expr = visitSuperSuffix(ctx.superSuffix());
        }
        else if (ctx.SUPER() != null) {
            expr = SimpleIdentifier.superIdentifier();
        }
        else if (ctx.explicitGenericInvocation() != null) {
            expr = visitExplicitGenericInvocation(ctx.explicitGenericInvocation());
        }
        else {
            throw new InternalParseException("Unknown scoped initializer", ctx.getText());
        }

        final Expression scope = visitExpression(ctx.expression(0));

        return new ScopedExpression(scope, expr);
    }

    private TernaryExpr visitTernaryExpression(final JavaParser.ExpressionContext ctx) {
        final List<Expression> expressions = visitNullableList(ctx.expression(), this::visitExpression);
        return new TernaryExpr(expressions.get(0), expressions.get(1), expressions.get(2));
    }

    private InstanceOfExpr<?> visitInstanceOfExpression(final JavaParser.ExpressionContext ctx) {
        final Expression left = visitExpression(ctx.expression(0));

        if (ctx.typeType(0) == null) {
            assert ctx.pattern() != null;
            return new InstanceOfExpr<>(left, conversionVisitor.visitPattern(ctx.pattern()));
        }
        else {
            return new InstanceOfExpr<>(left, conversionVisitor.visitTypeType(ctx.typeType(0)));
        }
    }

    private ArrayAccessExpr visitArrayAccessExpression(final JavaParser.ExpressionContext ctx) {
        final Expression array = visitExpression(ctx.expression(0));
        final Expression index = visitExpression(ctx.expression(1));
        return new ArrayAccessExpr(array, index);
    }

    private AssignmentExpr visitAssignmentExpression(final JavaParser.ExpressionContext ctx) {
        final Expression left = visitExpression(ctx.expression(0));
        final AssignmentOperator op = AssignmentOperator.tryFromString(ctx.assignOp.getText());
        final Expression right = visitExpression(ctx.expression(1));

        return new AssignmentExpr(left, op, right);
    }

    private BinaryExpr visitBinaryExpression(final JavaParser.ExpressionContext ctx) {
        final Expression left = visitExpression(ctx.expression(0));
        final BinaryOperator op = BinaryOperator.tryFromString(ctx.bop.getText());
        final Expression right = visitExpression(ctx.expression(1));

        return new BinaryExpr(left, op, right);
    }

    private BinaryExpr visitShiftExpression(final JavaParser.ExpressionContext ctx) {
        final BinaryOperator op;
        if (ctx.LT().size() == 2) {
            op = BinaryOperator.SHIFT_LEFT;
        }
        else if (ctx.GT().size() == 2) {
            op = BinaryOperator.SHIFT_RIGHT;
        }
        else if (ctx.GT().size() == 3) {
            op = BinaryOperator.UNSIGNED_SHIFT_RIGHT;
        }
        else {
            throw new InternalParseException("Missing shift guard/guarded operators from initializer!", ctx.getText());
        }

        final Expression left = visitExpression(ctx.expression(0));
        final Expression right = visitExpression(ctx.expression(1));

        return new BinaryExpr(left, op, right);
    }

    private MethodReference visitMethodReference(final JavaParser.ExpressionContext ctx) {
        final AstNode scope = getMethodReferenceScope(ctx);
        final Optional<TypeArguments> typeArguments = visitNullable(
            ctx.typeArguments(),
            conversionVisitor::visitTypeArguments
        );
        final SimpleIdentifier identifier;
        if (ctx.identifier() == null) {
            identifier = SimpleIdentifier.newIdentifier();
        }
        else {
            identifier = conversionVisitor.visitIdentifier(ctx.identifier());
        }

        return new MethodReference(scope, getNodes(typeArguments), identifier);
    }

    private AstNode getMethodReferenceScope(JavaParser.ExpressionContext ctx) {
        final AstNode scope;
        if (!ctx.typeType().isEmpty()) {
            scope = conversionVisitor.visitTypeType(ctx.typeType(0));
        }
        else if (ctx.classType() != null) {
            scope = conversionVisitor.visitClassType(ctx.classType());
        }
        else if (!ctx.expression().isEmpty()) {
            scope = visitExpression(ctx.expression(0));
        }
        else {
            throw new InternalParseException("Unknown method reference scope.", ctx.getText());
        }

        return scope;
    }

    private CastExpression visitCastExpression(final JavaParser.ExpressionContext ctx) {
        final List<Annotation> annotations = visitNullableList(ctx.annotation(), conversionVisitor::visitAnnotation);
        final Types targetTypes = new Types(visitNullableList(ctx.typeType(), conversionVisitor::visitTypeType));
        final JoinedTypes targetType = new JoinedTypes(JoinedTypes.JoinOperator.INTERSECTION, targetTypes.types());
        final Expression toCast = visitExpression(ctx.toCast);

        return new CastExpression(annotations, targetType, toCast);
    }

    private boolean hasShiftOperator(final JavaParser.ExpressionContext ctx) {
        final boolean hasLessThan = ctx.LT().size() > 1;
        final boolean hasGreaterThan = ctx.GT().size() > 1;
        return hasLessThan || hasGreaterThan;
    }
}
