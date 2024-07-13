// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.visitor;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.annotation.Annotation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.annotation.ElementValuePairs;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.annotation.ElementValues;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.annotation.NamedElementValue;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.annotation_declaration.AnnotationDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.annotation_declaration.AnnotationMethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.enum_declaration.EnumConstant;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.enum_declaration.EnumDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.interface_declaration.InterfaceDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.module_declaration.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.record_declaration.RecordComponent;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.record_declaration.RecordComponents;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.record_declaration.RecordDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.record_declaration.RecordInitializer;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.binary.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.creation.AnonymousClassCreation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.creation.ArrayCreation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.creation.ConstructorInvocation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.lambda.LambdaExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.lambda.LambdaParameters;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.literal.LiteralValueExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.literal.NullLiteralExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.pattern.AndPattern;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.pattern.BasePattern;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.pattern.GuardPattern;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.unary.UnaryExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.unary.UnaryOperator;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.GenericIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.QualifiedName;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.VariableDeclarationIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.parameter.Parameter;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.parameter.Parameters;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement.CatchClause;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement.Resources;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement.TryStmt;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.Switch;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.SwitchCase;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.labels.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.*;

public interface AstVisitor<T, A> {

    /**
     * Visits all children of the node and ignores the return value.
     *
     * @param node The node for which the children should be visited.
     * @param arg  The argument passed on to all children.
     */
    default void visitChildren(final AstNode node, final A arg) {
        visitChildren(node, arg, null, (a, b) -> null);
    }

    /**
     * Visits all children of the node and combines the return values.
     * <p>
     * Applies a stream reduction to the return values when visiting children. See
     * {@link java.util.stream.Stream#reduce(Object, java.util.function.BinaryOperator)} for an explanation how
     * {@code identity} and {@code accumulator} are used.
     *
     * @param node        The node for which the children should be visited.
     * @param arg         The argument passed on to all children.
     * @param identity    The identity for the accumulating function.
     * @param accumulator A function used to combine two return values of child-visits.
     * @return A combination of the return values obtained by visiting all children of {@code node}.
     */
    default T visitChildren(
        final AstNode node, final A arg, final T identity, final java.util.function.BinaryOperator<T> accumulator
    ) {
        return node
            .children()
            .stream()
            .map(c -> c.accept(this, arg))
            .reduce(identity, accumulator);
    }

    T visit(CompilationUnit node, A arg);

    T visit(Arguments node, A arg);

    T visit(Modifier node, A arg);

    T visit(Modifiers node, A arg);

    T visit(VariableModifier.FinalModifier node, A arg);

    T visit(VariableModifier.AnnotationModifier node, A arg);

    // region annotations

    T visit(Annotation node, A arg);

    T visit(ElementValuePairs node, A arg);

    T visit(ElementValues node, A arg);

    T visit(NamedElementValue node, A arg);

    // endregion annotations

    // region declaration

    T visit(AnnotationDeclaration node, A arg);

    T visit(AnnotationMethodDeclaration node, A arg);

    T visit(EnumConstant node, A arg);

    T visit(EnumDeclaration node, A arg);

    T visit(InterfaceDeclaration node, A arg);

    T visit(RecordComponent node, A arg);

    T visit(RecordComponents node, A arg);

    T visit(RecordDeclaration node, A arg);

    T visit(RecordInitializer node, A arg);

    <I extends AstNode> T visit(Body<I> node, A arg);

    T visit(ClassDeclaration node, A arg);

    T visit(ConstructorDeclaration node, A arg);

    T visit(FieldDeclaration node, A arg);

    T visit(ImportDeclaration node, A arg);

    <D extends AstNode> T visit(MemberDeclarator<D> node, A arg);

    T visit(MethodDeclaration node, A arg);

    T visit(ModuleDeclaration node, A arg);

    T visit(ModuleDeclarationBody node, A arg);

    T visit(ExportsModuleDirective node, A arg);

    T visit(OpensModuleDirective node, A arg);

    T visit(ProvidesModuleDirective node, A arg);

    T visit(RequiresModuleDirective node, A arg);

    T visit(RequiresModuleDirective.RequiresModifier node, A arg);

    T visit(UsesModuleDirective node, A arg);

    T visit(PackageDeclaration node, A arg);

    T visit(TypeDeclarator node, A arg);

    T visit(VariableDeclaration node, A arg);

    // endregion declaration

    // region expression

    T visit(ParExpression node, A arg);

    T visit(AssignmentExpr node, A arg);

    T visit(AssignmentOperator node, A arg);

    T visit(BinaryExpr node, A arg);

    T visit(BinaryOperator node, A arg);

    <R extends AstNode> T visit(InstanceOfExpr<R> node, A arg);

    T visit(AnonymousClassCreation node, A arg);

    T visit(ArrayCreation node, A arg);

    T visit(ConstructorInvocation node, A arg);

    T visit(LambdaExpr.BlockLambda node, A arg);

    T visit(LambdaExpr.ExprLambda node, A arg);

    T visit(LambdaParameters.IdentifierParameters node, A arg);

    <V> T visit(LiteralValueExpr<V> node, A arg);

    T visit(NullLiteralExpr node, A arg);

    T visit(AndPattern node, A arg);

    T visit(BasePattern node, A arg);

    T visit(GuardPattern node, A arg);

    T visit(UnaryExpr node, A arg);

    T visit(UnaryExpr.UnaryExprType node, A arg);

    T visit(UnaryOperator node, A arg);

    T visit(ArrayAccessExpr node, A arg);

    T visit(ArrayInitializer node, A arg);

    T visit(CastExpression node, A arg);

    T visit(ClassReference node, A arg);

    T visit(ExplicitGenericInvocation node, A arg);

    T visit(MethodInvocation node, A arg);

    T visit(MethodReference node, A arg);

    T visit(ScopedExpression node, A arg);

    T visit(SuperInvocation.SuperConstructorInvocation node, A arg);

    T visit(SuperInvocation.SuperMethodInvocation node, A arg);

    T visit(TernaryExpr node, A arg);

    T visit(ThisInvocation node, A arg);

    // endregion expression

    // region identifier

    T visit(GenericIdentifier node, A arg);

    T visit(QualifiedName node, A arg);

    T visit(SimpleIdentifier node, A arg);

    T visit(VariableDeclarationIdentifier.ArrayVariableIdentifier node, A arg);

    // endregion identifier

    // region parameter

    T visit(Parameters node, A arg);

    T visit(Parameter.ReceiverParameter node, A arg);

    T visit(Parameter.FormalParameter node, A arg);

    T visit(Parameter.VarParameter node, A arg);

    // endregion parameter

    // region switch

    T visit(Switch.SwitchExpr node, A arg);

    T visit(Switch.SwitchStmt node, A arg);

    T visit(SwitchCase node, A arg);

    T visit(ChoiceLabel node, A arg);

    T visit(DefaultLabel node, A arg);

    T visit(EnumLabel node, A arg);

    T visit(MultipleLabel node, A arg);

    T visit(PatternLabel node, A arg);

    T visit(SimpleLabel node, A arg);

    // endregion switch

    // region statement

    T visit(CatchClause node, A arg);

    T visit(Resources node, A arg);

    T visit(Resources.DeclaredResource node, A arg);

    T visit(Resources.ExpressionResource node, A arg);

    T visit(TryStmt.TryWithResources node, A arg);

    T visit(TryStmt.RegularTry node, A arg);

    T visit(AssertStmt node, A arg);

    T visit(Block node, A arg);

    T visit(BreakStmt node, A arg);

    T visit(ContinueStmt node, A arg);

    T visit(DoWhileStmt node, A arg);

    T visit(EmptyStmt node, A arg);

    T visit(ExpressionStmt node, A arg);

    T visit(ForStmt node, A arg);

    T visit(ForStmt.RegularFor node, A arg);

    T visit(ForStmt.EnhancedFor node, A arg);

    T visit(IfStmt node, A arg);

    T visit(LabelledStmt node, A arg);

    T visit(LocalTypeDeclarationStmt node, A arg);

    T visit(LocalVariableDeclStmt.LocalVarVariableDecl node, A arg);

    T visit(LocalVariableDeclStmt.LocalTypedVariableDecl node, A arg);

    T visit(ReturnStmt node, A arg);

    T visit(SynchronizedStmt node, A arg);

    T visit(ThrowStmt node, A arg);

    T visit(WhileStmt node, A arg);

    T visit(YieldStmt node, A arg);

    // endregion statement

    // region type

    T visit(QualifiedGenericType node, A arg);

    T visit(ClassType node, A arg);

    T visit(JoinedTypes node, A arg);

    T visit(JoinedTypes.JoinOperator node, A arg);

    T visit(PrimitiveType node, A arg);

    T visit(Type.VoidType node, A arg);

    T visit(Type.Var node, A arg);

    T visit(TypeArgument.WildcardType node, A arg);

    T visit(TypeArgument.WildcardTypeRestriction node, A arg);

    T visit(TypeParameter node, A arg);

    T visit(TypeParameter.Bound node, A arg);

    T visit(ComplexType node, A arg);

    T visit(ComplexType.ArrayType node, A arg);

    // endregion type
}
