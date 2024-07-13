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

/**
 * All visit methods call {@link #defaultAction(AstNode, Object)}.
 *
 * @param <T> The type of the values returned from visit methods.
 * @param <A> The type of the additional argument to the visit methods.
 */
public interface AstVisitorWithDefaults<T, A> extends AstVisitor<T, A> {

    /**
     * The default action that is called if an overriding visitor implementation does not implement a specific visit
     * method.
     *
     * @param node The node of the AST that is visited.
     * @param arg  Some additional argument.
     * @return The default implementation visits all children and returns {@code null}.
     */
    default T defaultAction(AstNode node, A arg) {
        visitChildren(node, arg);
        return null;
    }

    @Override
    default T visit(CompilationUnit node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(Arguments node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(Modifier node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(Modifiers node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(VariableModifier.FinalModifier node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(VariableModifier.AnnotationModifier node, A arg) {
        return defaultAction(node, arg);
    }

    // region annotations

    @Override
    default T visit(Annotation node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ElementValuePairs node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ElementValues node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(NamedElementValue node, A arg) {
        return defaultAction(node, arg);
    }

    // endregion annotations

    // region declaration

    @Override
    default T visit(AnnotationDeclaration node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(AnnotationMethodDeclaration node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(EnumConstant node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(EnumDeclaration node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(InterfaceDeclaration node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(RecordComponent node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(RecordComponents node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(RecordDeclaration node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(RecordInitializer node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default <I extends AstNode> T visit(Body<I> node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ClassDeclaration node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ConstructorDeclaration node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(FieldDeclaration node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ImportDeclaration node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default <D extends AstNode> T visit(MemberDeclarator<D> node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(MethodDeclaration node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ModuleDeclaration node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ModuleDeclarationBody node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ExportsModuleDirective node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(OpensModuleDirective node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ProvidesModuleDirective node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(RequiresModuleDirective node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(RequiresModuleDirective.RequiresModifier node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(UsesModuleDirective node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(PackageDeclaration node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(TypeDeclarator node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(VariableDeclaration node, A arg) {
        return defaultAction(node, arg);
    }

    // endregion declaration

    // region expression

    @Override
    default T visit(ParExpression node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(AssignmentExpr node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(AssignmentOperator node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(BinaryExpr node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(BinaryOperator node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default <R extends AstNode> T visit(InstanceOfExpr<R> node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(AnonymousClassCreation node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ArrayCreation node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ConstructorInvocation node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(LambdaExpr.BlockLambda node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(LambdaExpr.ExprLambda node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(LambdaParameters.IdentifierParameters node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default <V> T visit(LiteralValueExpr<V> node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(NullLiteralExpr node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(AndPattern node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(BasePattern node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(GuardPattern node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(UnaryExpr node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(UnaryExpr.UnaryExprType node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(UnaryOperator node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ArrayAccessExpr node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ArrayInitializer node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(CastExpression node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ClassReference node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ExplicitGenericInvocation node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(MethodInvocation node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(MethodReference node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ScopedExpression node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(SuperInvocation.SuperConstructorInvocation node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(SuperInvocation.SuperMethodInvocation node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(TernaryExpr node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ThisInvocation node, A arg) {
        return defaultAction(node, arg);
    }

    // endregion expression

    // region identifier

    @Override
    default T visit(GenericIdentifier node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(QualifiedName node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(SimpleIdentifier node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(VariableDeclarationIdentifier.ArrayVariableIdentifier node, A arg) {
        return defaultAction(node, arg);
    }

    // endregion identifier

    // region parameter

    @Override
    default T visit(Parameters node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(Parameter.ReceiverParameter node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(Parameter.FormalParameter node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(Parameter.VarParameter node, A arg) {
        return defaultAction(node, arg);
    }

    // endregion parameter

    // region switch

    @Override
    default T visit(Switch.SwitchExpr node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(Switch.SwitchStmt node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(SwitchCase node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ChoiceLabel node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(DefaultLabel node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(EnumLabel node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(MultipleLabel node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(PatternLabel node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(SimpleLabel node, A arg) {
        return defaultAction(node, arg);
    }

    // endregion switch

    // region statement

    @Override
    default T visit(CatchClause node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(Resources node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(Resources.DeclaredResource node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(Resources.ExpressionResource node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(TryStmt.TryWithResources node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(TryStmt.RegularTry node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(AssertStmt node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(Block node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(BreakStmt node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ContinueStmt node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(DoWhileStmt node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(EmptyStmt node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ExpressionStmt node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ForStmt node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ForStmt.RegularFor node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ForStmt.EnhancedFor node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(IfStmt node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(LabelledStmt node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(LocalTypeDeclarationStmt node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(LocalVariableDeclStmt.LocalVarVariableDecl node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(LocalVariableDeclStmt.LocalTypedVariableDecl node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ReturnStmt node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(SynchronizedStmt node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ThrowStmt node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(WhileStmt node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(YieldStmt node, A arg) {
        return defaultAction(node, arg);
    }

    // endregion statement

    // region type

    @Override
    default T visit(QualifiedGenericType node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ClassType node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(JoinedTypes node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(JoinedTypes.JoinOperator node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(PrimitiveType node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(Type.VoidType node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(Type.Var node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(TypeArgument.WildcardType node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(TypeArgument.WildcardTypeRestriction node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(TypeParameter node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(TypeParameter.Bound node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ComplexType node, A arg) {
        return defaultAction(node, arg);
    }

    @Override
    default T visit(ComplexType.ArrayType node, A arg) {
        return defaultAction(node, arg);
    }

    // endregion type
}
