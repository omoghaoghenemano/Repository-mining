// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.parser;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.Modifiers;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.VariableModifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.ClassMemberDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.Expression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.pattern.BasePattern;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.QualifiedName;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement.CatchClause;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement.Resources;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement.TryStmt;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.Switch;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.SwitchCase;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.labels.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.ClassOrInterfaceType;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.JoinedTypes;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.Type;
import de.uni_passau.fim.se2.sa.ggnn.javaparser.JavaParser;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

class AntlrStatementAstConversionVisitor extends AntlrAstBaseConversionVisitor implements AntlrAstConverter {

    private final AntlrAstConversionVisitor conversionVisitor;

    AntlrStatementAstConversionVisitor(final AntlrAstConversionVisitor conversionVisitor) {
        this.conversionVisitor = conversionVisitor;
    }

    @Override
    public Statement visitBlockStatement(JavaParser.BlockStatementContext ctx) {
        checkErrorNode("block statement", ctx);

        if (ctx.statement() != null) {
            return visitStatement(ctx.statement());
        }
        else if (ctx.switchExpression() != null) {
            final Switch.SwitchExpr expr = conversionVisitor.visitSwitchExpression(ctx.switchExpression());
            return new ExpressionStmt(expr);
        }
        else if (ctx.localTypeDeclaration() != null) {
            return visitLocalTypeDeclaration(ctx.localTypeDeclaration());
        }
        else if (ctx.localVariableDeclaration() != null) {
            return visitLocalVariableDeclaration(ctx.localVariableDeclaration());
        }
        else if (ctx.yieldStatement() != null) {
            return visitYieldStatement(ctx.yieldStatement());
        }

        throw new InternalParseException("Unknown Block-Statement.", ctx.getText());
    }

    @Override
    public LocalVariableDeclStmt visitLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx) {
        checkErrorNode("local variable declaration", ctx);

        final List<VariableModifier> modifiers = visitNullableList(
            ctx.variableModifier(),
            conversionVisitor::visitVariableModifier
        );
        if (ctx.VAR() == null) {
            final Type type = conversionVisitor.visitTypeType(ctx.typeType());
            final VariableDeclarations decl = conversionVisitor.visitVariableDeclarators(ctx.variableDeclarators());
            return new LocalVariableDeclStmt.LocalTypedVariableDecl(modifiers, type, decl.declarations());
        }
        else {
            final Identifier identifier = conversionVisitor.visitIdentifier(ctx.identifier());
            final Expression expression = conversionVisitor.visitExpression(ctx.expression());
            return new LocalVariableDeclStmt.LocalVarVariableDecl(modifiers, identifier, expression);
        }
    }

    @Override
    public Statement visitLocalTypeDeclaration(JavaParser.LocalTypeDeclarationContext ctx) {
        checkErrorNode("local type declaration", ctx);

        final ClassMemberDeclaration declaration;
        if (ctx.classDeclaration() != null) {
            declaration = conversionVisitor.visitClassDeclaration(ctx.classDeclaration());
        }
        else if (ctx.interfaceDeclaration() != null) {
            declaration = conversionVisitor.visitInterfaceDeclaration(ctx.interfaceDeclaration());
        }
        else if (ctx.recordDeclaration() != null) {
            declaration = conversionVisitor.visitRecordDeclaration(ctx.recordDeclaration());
        }
        else {
            throw new InternalParseException("Unknown ClassMemberDeclaration", ctx.getText());
        }

        final List<Modifiers> modifiers = visitNullableList(
            ctx.classOrInterfaceModifier(),
            conversionVisitor::visitClassOrInterfaceModifier
        );

        return new LocalTypeDeclarationStmt(modifiers, declaration);
    }

    @Override
    @SuppressWarnings({ "checkstyle:cyclomaticComplexity", "PMD.CognitiveComplexity" })
    public Statement visitStatement(JavaParser.StatementContext ctx) {
        checkErrorNode("statement", ctx);

        if (ctx.statementExpression != null) {
            return new ExpressionStmt(conversionVisitor.visitExpression(ctx.statementExpression));
        }
        else if (ctx.identifierLabel != null) {
            return visitLabelledStatement(ctx);
        }
        else if (ctx.TRY() != null) {
            return visitTryStmt(ctx);
        }
        else if (ctx.SYNCHRONIZED() != null) {
            return visitSynchronizedStmt(ctx);
        }
        else if (ctx.block() != null) {
            return visitBlockStatement(ctx);
        }
        else if (ctx.ASSERT() != null) {
            return visitAssertStmt(ctx);
        }
        else if (ctx.IF() != null) {
            return visitIfStmt(ctx);
        }
        else if (ctx.FOR() != null) {
            return visitForStmt(ctx);
        }
        else if (ctx.DO() != null) {
            return visitDoWhileStmt(ctx);
        }
        else if (ctx.WHILE() != null) {
            return visitWhileStmt(ctx);
        }
        else if (ctx.SWITCH() != null) {
            return visitSwitchStmt(ctx);
        }
        else if (ctx.RETURN() != null) {
            return visitReturnStmt(ctx);
        }
        else if (ctx.THROW() != null) {
            return visitThrowStmt(ctx);
        }
        else if (ctx.BREAK() != null) {
            return visitBreakStmt(ctx);
        }
        else if (ctx.CONTINUE() != null) {
            return visitContinueStmt(ctx);
        }
        else if (ctx.yieldStatement() != null) {
            return visitYieldStatement(ctx.yieldStatement());
        }
        else if (ctx.SEMI() != null) {
            return visitEmptyStatement();
        }

        throw new InternalParseException("Unknown block statement.", ctx.getText());
    }

    private Block visitBlockStatement(final JavaParser.StatementContext ctx) {
        final List<Statement> statements = visitNullableList(ctx.block().blockStatement(), this::visitBlockStatement);
        return new Block(statements);
    }

    @Override
    public YieldStmt visitYieldStatement(JavaParser.YieldStatementContext ctx) {
        checkErrorNode("yield statement", ctx);

        final Expression expression = conversionVisitor.visitExpression(ctx.expression());
        return new YieldStmt(expression);
    }

    @Override
    public CatchClause visitCatchClause(JavaParser.CatchClauseContext ctx) {
        checkErrorNode("catch clause", ctx);

        final List<VariableModifier> modifiers = visitNullableList(
            ctx.variableModifier(),
            conversionVisitor::visitVariableModifier
        );
        final Type catchType = visitCatchType(ctx.catchType());
        final Identifier identifier = conversionVisitor.visitIdentifier(ctx.identifier());
        final Block catchBlock = conversionVisitor.visitBlock(ctx.block());

        return new CatchClause(modifiers, catchType, identifier, catchBlock);
    }

    @Override
    public Type visitCatchType(JavaParser.CatchTypeContext ctx) {
        checkErrorNode("catch exception type", ctx);

        // If only one qualified name in catch clause exists, we return a ClassOrInterfaceType.
        if (ctx.qualifiedName().size() == 1) {
            return buildTypeFromIdentifier(conversionVisitor.visitQualifiedName(ctx.qualifiedName(0)));
        }
        else {
            // If more than one qualified name in catch clause exists, we return a joined type with union operator.
            final List<Type> qualifiedNames = visitNullableList(
                ctx.qualifiedName(),
                conversionVisitor::visitQualifiedName
            )
                .stream()
                .map(Type.class::cast)
                .toList();

            return new JoinedTypes(JoinedTypes.JoinOperator.UNION, qualifiedNames);
        }
    }

    private ClassOrInterfaceType buildTypeFromIdentifier(final Identifier identifier) {
        if (identifier instanceof SimpleIdentifier id) {
            return id;
        }
        else if (identifier instanceof QualifiedName q) {
            return q;
        }
        else {
            throw new InternalParseException("Cannot interpret the identifier '" + identifier + "' as class type.");
        }
    }

    @Override
    public Block visitFinallyBlock(JavaParser.FinallyBlockContext ctx) {
        checkErrorNode("finally block", ctx);
        return conversionVisitor.visitBlock(ctx.block());
    }

    @Override
    public Resources visitResourceSpecification(JavaParser.ResourceSpecificationContext ctx) {
        checkErrorNode("try resources", ctx);
        return visitResources(ctx.resources());
    }

    @Override
    public Resources visitResources(JavaParser.ResourcesContext ctx) {
        checkErrorNode("try resources", ctx);
        return new Resources(visitNullableList(ctx.resource(), this::visitResource));
    }

    @Override
    public Resources.Resource visitResource(JavaParser.ResourceContext ctx) {
        checkErrorNode("resource", ctx);

        if (ctx.identifier() == null && ctx.variableModifier().isEmpty() && ctx.classOrInterfaceType() == null) {
            return new Resources.ExpressionResource(conversionVisitor.visitExpression(ctx.expression()));
        }

        final Type type;
        final Identifier identifier;
        if (ctx.VAR() == null) {
            type = conversionVisitor.visitClassOrInterfaceType(ctx.classOrInterfaceType());
            identifier = conversionVisitor.visitVariableDeclaratorId(ctx.variableDeclaratorId());
        }
        else {
            type = new Type.Var();
            identifier = conversionVisitor.visitIdentifier(ctx.identifier());
        }

        final List<VariableModifier> modifiers = visitNullableList(
            ctx.variableModifier(),
            conversionVisitor::visitVariableModifier
        );
        final Expression expression = conversionVisitor.visitExpression(ctx.expression());

        return new Resources.DeclaredResource(modifiers, type, identifier, expression);
    }

    @Override
    public SwitchCase visitSwitchBlockStatementGroup(JavaParser.SwitchBlockStatementGroupContext ctx) {
        checkErrorNode("switch block", ctx);

        final List<SwitchLabel> switchLabels = visitNullableList(ctx.switchLabel(), this::visitSwitchLabel);
        final List<Statement> statements = visitNullableList(
            ctx.blockStatement(), conversionVisitor::visitBlockStatement
        );
        final Block block = new Block(statements);
        if (switchLabels.size() == 1) {
            return new SwitchCase(switchLabels.get(0), block);
        }
        else {
            return new SwitchCase(new MultipleLabel(switchLabels), new Block(statements));
        }
    }

    @Override
    public SwitchLabel visitSwitchLabel(JavaParser.SwitchLabelContext ctx) {
        checkErrorNode("switch label", ctx);

        if (ctx.DEFAULT() != null) {
            return new DefaultLabel();
        }
        else if (ctx.IDENTIFIER() != null) {
            return new EnumLabel(new SimpleIdentifier(ctx.IDENTIFIER().getText()));
        }
        else if (ctx.typeType() != null) {
            final Type type = conversionVisitor.visitTypeType(ctx.typeType());
            final SimpleIdentifier id = conversionVisitor.visitIdentifier(ctx.identifier());
            final BasePattern pattern = new BasePattern(
                Collections.emptyList(), type, Collections.emptyList(), id
            );

            return new PatternLabel(pattern);
        }
        else if (ctx.expression() != null) {
            return new SimpleLabel(conversionVisitor.visitExpression(ctx.expression()));
        }

        throw new InternalParseException("Unknown switch label", ctx.getText());
    }

    @Override
    public ForStmt.ForControl visitForControl(JavaParser.ForControlContext ctx) {
        checkErrorNode("for header", ctx);

        if (ctx.enhancedForControl() == null) {
            return visitForSimpleControlStmt(ctx);
        }
        else {
            return visitEnhancedForControl(ctx.enhancedForControl());
        }
    }

    @Override
    public AstNode visitForInit(JavaParser.ForInitContext ctx) {
        checkErrorNode("for init", ctx);

        if (ctx.localVariableDeclaration() == null) {
            return conversionVisitor.visitExpressionList(ctx.expressionList());
        }
        else {
            return conversionVisitor.visitLocalVariableDeclaration(ctx.localVariableDeclaration());
        }
    }

    @Override
    public ForStmt.EnhancedFor visitEnhancedForControl(JavaParser.EnhancedForControlContext ctx) {
        checkErrorNode("enhanced for header", ctx);

        final Type type;
        if (ctx.VAR() == null) {
            type = conversionVisitor.visitTypeType(ctx.typeType());
        }
        else {
            type = new Type.Var();
        }
        final var identifier = conversionVisitor.visitVariableDeclaratorId(ctx.variableDeclaratorId());
        final var expression = conversionVisitor.visitExpression(ctx.expression());

        return new ForStmt.EnhancedFor(type, identifier, expression);
    }

    private Block visitControlStructureBody(final JavaParser.StatementContext ctx) {
        final Statement stmt = visitStatement(ctx);
        if (stmt instanceof Block block) {
            return block;
        }
        else {
            return new Block(List.of(stmt));
        }
    }

    private AssertStmt visitAssertStmt(JavaParser.StatementContext ctx) {
        final JavaParser.ExpressionContext assertExpressCtx = ctx.expression(0);
        final Optional<Expression> messageExpression = visitNullable(
            ctx.expression(1), conversionVisitor::visitExpression
        );

        return new AssertStmt(conversionVisitor.visitExpression(assertExpressCtx), messageExpression);
    }

    private IfStmt visitIfStmt(JavaParser.StatementContext ctx) {
        final Expression expression = conversionVisitor.visitParExpression(ctx.parExpression());
        final Block thenStatement = visitControlStructureBody(ctx.statement(0));
        final Optional<Block> elseStatementOpt = visitNullable(ctx.statement(1), this::visitControlStructureBody);

        return new IfStmt(expression, thenStatement, elseStatementOpt);
    }

    private ForStmt visitForStmt(JavaParser.StatementContext ctx) {
        final var forControlStmt = visitForControl(ctx.forControl());
        return new ForStmt(forControlStmt, visitControlStructureBody(ctx.statement(0)));
    }

    private ForStmt.RegularFor visitForSimpleControlStmt(JavaParser.ForControlContext ctx) {
        final var forInitStmt = visitNullable(ctx.forInit(), conversionVisitor::visitForInit);
        final List<AstNode> forInit;
        if (forInitStmt.isPresent() && forInitStmt.get() instanceof Expressions expressions) {
            forInit = expressions.children();
        }
        else {
            forInit = forInitStmt.stream().toList();
        }

        final var conditionExpression = visitNullable(ctx.expression(), conversionVisitor::visitExpression);
        final var updateExpression = visitNullable(ctx.forUpdate, conversionVisitor::visitExpressionList);

        return new ForStmt.RegularFor(forInit, conditionExpression, getNodes(updateExpression));
    }

    private WhileStmt visitWhileStmt(JavaParser.StatementContext ctx) {
        final Expression condition = conversionVisitor.visitParExpression(ctx.parExpression());
        return new WhileStmt(condition, visitControlStructureBody(ctx.statement(0)));

    }

    private DoWhileStmt visitDoWhileStmt(JavaParser.StatementContext ctx) {
        final Expression condition = conversionVisitor.visitParExpression(ctx.parExpression());
        return new DoWhileStmt(visitControlStructureBody(ctx.statement(0)), condition);
    }

    private TryStmt visitTryStmt(JavaParser.StatementContext ctx) {
        final Block block = conversionVisitor.visitBlock(ctx.block());
        final List<CatchClause> catchClauses = visitNullableList(ctx.catchClause(), this::visitCatchClause);
        final Optional<Block> finallyStmt = visitNullable(ctx.finallyBlock(), this::visitFinallyBlock);
        if (ctx.resourceSpecification() == null) {
            return new TryStmt.RegularTry(block, catchClauses, finallyStmt);
        }
        else {
            final Resources resourceSpecStmt = visitResourceSpecification(ctx.resourceSpecification());
            return new TryStmt.TryWithResources(resourceSpecStmt, block, catchClauses, finallyStmt);
        }
    }

    private SynchronizedStmt visitSynchronizedStmt(JavaParser.StatementContext ctx) {
        final Expression parExpression = conversionVisitor.visitParExpression(ctx.parExpression());
        final Block block = conversionVisitor.visitBlock(ctx.block());
        return new SynchronizedStmt(parExpression, block);
    }

    private Switch.SwitchStmt visitSwitchStmt(JavaParser.StatementContext ctx) {
        final Expression check = conversionVisitor.visitParExpression(ctx.parExpression());
        final List<SwitchCase> switchCases = visitNullableList(
            ctx.switchBlockStatementGroup(),
            this::visitSwitchBlockStatementGroup
        );
        return new Switch.SwitchStmt(check, switchCases);
    }

    private ReturnStmt visitReturnStmt(JavaParser.StatementContext ctx) {
        return new ReturnStmt(visitNullable(ctx.expression(0), conversionVisitor::visitExpression));
    }

    private ThrowStmt visitThrowStmt(JavaParser.StatementContext ctx) {
        return new ThrowStmt(conversionVisitor.visitExpression(ctx.expression(0)));
    }

    private BreakStmt visitBreakStmt(JavaParser.StatementContext ctx) {
        final Optional<Identifier> posIdentifier = visitNullable(ctx.identifier(), conversionVisitor::visitIdentifier);
        return new BreakStmt(posIdentifier);
    }

    private ContinueStmt visitContinueStmt(JavaParser.StatementContext ctx) {
        final Optional<Identifier> posIdentifier = visitNullable(ctx.identifier(), conversionVisitor::visitIdentifier);
        return new ContinueStmt(posIdentifier);
    }

    private EmptyStmt visitEmptyStatement() {
        return new EmptyStmt();
    }

    private LabelledStmt visitLabelledStatement(JavaParser.StatementContext ctx) {
        final Identifier identifier = conversionVisitor.visitIdentifier(ctx.identifier());
        final Statement statement = visitStatement(ctx.statement(0));
        return new LabelledStmt(identifier, statement);
    }
}
