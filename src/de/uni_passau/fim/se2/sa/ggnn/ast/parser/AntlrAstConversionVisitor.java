// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.parser;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.annotation.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.annotation_declaration.AnnotationDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.annotation_declaration.AnnotationMemberDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.annotation_declaration.AnnotationMethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.enum_declaration.EnumConstant;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.enum_declaration.EnumDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.interface_declaration.InterfaceDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.interface_declaration.InterfaceMemberDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.module_declaration.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.record_declaration.RecordComponent;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.record_declaration.RecordComponents;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.record_declaration.RecordDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.record_declaration.RecordInitializer;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.ArrayInitializer;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.Expression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.InvocationExpression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.SuperInvocation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.creation.ArrayCreation;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.creation.ClassCreationExpression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.lambda.LambdaExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.lambda.LambdaParameters;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.literal.LiteralExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.literal.LiteralValueExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.literal.NullLiteralExpr;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.pattern.AndPattern;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.pattern.BasePattern;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.pattern.GuardPattern;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.pattern.Pattern;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.MaybeGenericIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.VariableDeclarationIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.parameter.Parameter;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.parameter.Parameters;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.Block;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.ForStmt;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.LocalVariableDeclStmt;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.Statement;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement.CatchClause;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement.Resources;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.Switch;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.SwitchCase;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.labels.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.*;
import de.uni_passau.fim.se2.sa.ggnn.javaparser.JavaParser;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Stream;

class AntlrAstConversionVisitor extends AntlrAstBaseConversionVisitor implements AntlrAstConverter {

    private static final String LONG_MIN_VALUE_EXPR_BIN = "0b1000000000000000000000000000000000000000000000000000000000000000";
    private static final String LONG_MIN_VALUE_EXPR_OCT = "01000000000000000000000";
    private static final String LONG_MIN_VALUE_EXPR_DEC = "-9223372036854775808";
    private static final String LONG_MIN_VALUE_EXPR_HEX = "0x8000000000000000";

    private final AntlrAnnotationAstConversionVisitor annotationAstConversionVisitor = new AntlrAnnotationAstConversionVisitor(
        this
    );
    private final AntlrExpressionAstConversionVisitor expressionAstConversionVisitor = new AntlrExpressionAstConversionVisitor(
        this
    );
    private final AntlrStatementAstConversionVisitor statementAstConversionVisitor = new AntlrStatementAstConversionVisitor(
        this
    );

    /**
     * Visits the parse tree of a compilation unit.
     *
     * @param ctx the parse tree
     * @return Either a {@link ModuleDeclaration
     *         ModuleDeclaration} or a {@link CompilationUnit}.
     */
    @Override
    public AstNode visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
        checkErrorNode("compilation unit", ctx);

        if (ctx.moduleDeclaration() == null) {
            final Optional<PackageDeclaration> packageDeclaration = visitNullable(
                ctx.packageDeclaration(),
                this::visitPackageDeclaration
            );
            final List<ImportDeclaration> importDeclarations = visitNullableList(
                ctx.importDeclaration(),
                this::visitImportDeclaration
            );
            final List<TypeDeclarator> typeDeclarations = visitNullableList(
                ctx.typeDeclaration(),
                this::visitTypeDeclaration
            );

            return new CompilationUnit(packageDeclaration, importDeclarations, typeDeclarations);
        }
        else {
            return visitModuleDeclaration(ctx.moduleDeclaration());
        }
    }

    @Override
    public PackageDeclaration visitPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
        checkErrorNode("package declaration", ctx);

        final List<Annotation> annotations = visitNullableList(ctx.annotation(), this::visitAnnotation);
        final Identifier name = visitQualifiedName(ctx.qualifiedName());
        return new PackageDeclaration(annotations, name);
    }

    @Override
    public ImportDeclaration visitImportDeclaration(JavaParser.ImportDeclarationContext ctx) {
        checkErrorNode("import declaration", ctx);

        final boolean isStatic = ctx.STATIC() != null;
        final Identifier name = visitQualifiedName(ctx.qualifiedName());
        final boolean isStarImport = ctx.MUL() != null;

        return new ImportDeclaration(isStatic, name, isStarImport);
    }

    @Override
    public TypeDeclarator visitTypeDeclaration(JavaParser.TypeDeclarationContext ctx) {
        checkErrorNode("type declaration", ctx);

        final TypeDeclaration declaration;
        if (ctx.classDeclaration() != null) {
            declaration = visitClassDeclaration(ctx.classDeclaration());
        }
        else if (ctx.enumDeclaration() != null) {
            declaration = visitEnumDeclaration(ctx.enumDeclaration());
        }
        else if (ctx.interfaceDeclaration() != null) {
            declaration = visitInterfaceDeclaration(ctx.interfaceDeclaration());
        }
        else if (ctx.annotationTypeDeclaration() != null) {
            declaration = visitAnnotationTypeDeclaration(ctx.annotationTypeDeclaration());
        }
        else if (ctx.recordDeclaration() != null) {
            declaration = visitRecordDeclaration(ctx.recordDeclaration());
        }
        else {
            throw new InternalParseException("Unknown type declaration: ", ctx.getText());
        }

        final Modifiers modifiers = visitModifiers(ctx.classOrInterfaceModifier());

        return new TypeDeclarator(modifiers, declaration);
    }

    private Modifiers visitModifiers(List<JavaParser.ClassOrInterfaceModifierContext> ctx) {
        return Modifiers.merge(ctx.stream().map(this::visitClassOrInterfaceModifier));
    }

    @Override
    public Modifiers visitModifier(JavaParser.ModifierContext ctx) {
        checkErrorNode("modifier", ctx);

        if (ctx.classOrInterfaceModifier() == null) {
            final Modifier m = Modifier.tryFromString(ctx.getText());
            return new Modifiers(Collections.emptyList(), List.of(m));
        }
        else {
            return visitClassOrInterfaceModifier(ctx.classOrInterfaceModifier());
        }
    }

    @Override
    public Modifiers visitClassOrInterfaceModifier(JavaParser.ClassOrInterfaceModifierContext ctx) {
        if (ctx.annotation() == null) {
            return new Modifiers(Collections.emptyList(), List.of(visitClassModifier(ctx)));
        }
        else {
            return new Modifiers(List.of(visitAnnotation(ctx.annotation())), Collections.emptyList());
        }
    }

    private Modifier visitClassModifier(JavaParser.ClassOrInterfaceModifierContext ctx) {
        if (ctx.PUBLIC() != null) {
            return Modifier.PUBLIC;
        }
        else if (ctx.PROTECTED() != null) {
            return Modifier.PROTECTED;
        }
        else if (ctx.PRIVATE() != null) {
            return Modifier.PRIVATE;
        }
        else if (ctx.STATIC() != null) {
            return Modifier.STATIC;
        }
        else if (ctx.ABSTRACT() != null) {
            return Modifier.ABSTRACT;
        }
        else if (ctx.FINAL() != null) {
            return Modifier.FINAL;
        }
        else if (ctx.STRICTFP() != null) {
            return Modifier.STRICTFP;
        }
        else if (ctx.SEALED() != null) {
            return Modifier.SEALED;
        }
        else if (ctx.NON_SEALED() != null) {
            return Modifier.NON_SEALED;
        }
        else {
            throw new InternalParseException("Unknown class modifier: " + ctx.getText());
        }
    }

    @Override
    public VariableModifier visitVariableModifier(JavaParser.VariableModifierContext ctx) {
        checkErrorNode("variable modifier", ctx);

        if (ctx.annotation() != null) {
            return new VariableModifier.AnnotationModifier(visitAnnotation(ctx.annotation()));
        }
        else if (ctx.FINAL() != null) {
            return new VariableModifier.FinalModifier();
        }
        else {
            throw new InternalParseException("Unknown variable modifier: " + ctx.getText());
        }
    }

    @Override
    public ClassDeclaration visitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
        checkErrorNode("class declaration", ctx);

        final SimpleIdentifier name = visitIdentifier(ctx.identifier());
        final Optional<TypeParameters> typeParameters = visitNullable(ctx.typeParameters(), this::visitTypeParameters);
        final Optional<Type> extendsType = visitNullable(ctx.typeType(), this::visitTypeType);
        final Types implementsTypes = visitNullable(ctx.implementsTypes, this::visitTypeList).orElse(Types.empty());
        final Types permitsTypes = visitNullable(ctx.permitsTypes, this::visitTypeList).orElse(Types.empty());
        final Body<ClassBodyDeclaration> body = visitClassBody(ctx.classBody());

        return new ClassDeclaration(
            name,
            getNodes(typeParameters),
            extendsType,
            implementsTypes.types(),
            permitsTypes.types(),
            body
        );
    }

    @Override
    public TypeParameters visitTypeParameters(JavaParser.TypeParametersContext ctx) {
        checkErrorNode("type parameters", ctx);

        final List<TypeParameter> parameters = visitNullableList(ctx.typeParameter(), this::visitTypeParameter);
        return new TypeParameters(parameters);
    }

    @Override
    public TypeParameter visitTypeParameter(JavaParser.TypeParameterContext ctx) {
        checkErrorNode("type parameter", ctx);

        final Optional<Annotation> annotation = visitNullable(ctx.selfAnnotation, this::visitAnnotation);
        final SimpleIdentifier identifier = visitIdentifier(ctx.identifier());
        final Optional<TypeParameter.Bound> bound = Optional.ofNullable(ctx.EXTENDS()).map(e -> {
            final Optional<Annotation> extendsAnnotation = visitNullable(ctx.extendsAnnotation, this::visitAnnotation);
            final TypeBound typeBound = visitTypeBound(ctx.typeBound());
            return new TypeParameter.Bound(extendsAnnotation, typeBound.bounds());
        });

        return new TypeParameter(annotation, identifier, bound);
    }

    @Override
    public TypeBound visitTypeBound(JavaParser.TypeBoundContext ctx) {
        checkErrorNode("type bound", ctx);

        final List<Type> bounds = visitNullableList(ctx.typeType(), this::visitTypeType);
        return new TypeBound(bounds);
    }

    @Override
    public EnumDeclaration visitEnumDeclaration(JavaParser.EnumDeclarationContext ctx) {
        checkErrorNode("enum declaration", ctx);

        final SimpleIdentifier name = visitIdentifier(ctx.identifier());
        final Types implementsTypes = visitNullable(ctx.typeList(), this::visitTypeList).orElse(Types.empty());
        final Body<EnumConstant> constants = visitNullable(ctx.enumConstants(), this::visitEnumConstants)
            .orElse(new Body<>(Collections.emptyList()));
        final Body<ClassBodyDeclaration> declarations = visitNullable(
            ctx.enumBodyDeclarations(),
            this::visitEnumBodyDeclarations
        ).orElse(new Body<>(Collections.emptyList()));

        return new EnumDeclaration(name, implementsTypes.types(), constants, declarations);
    }

    @Override
    public Body<EnumConstant> visitEnumConstants(JavaParser.EnumConstantsContext ctx) {
        checkErrorNode("enum constants", ctx);

        final List<EnumConstant> constants = visitNullableList(ctx.enumConstant(), this::visitEnumConstant);
        return new Body<>(constants);
    }

    @Override
    public EnumConstant visitEnumConstant(JavaParser.EnumConstantContext ctx) {
        checkErrorNode("enum constant", ctx);

        final List<Annotation> annotations = visitNullableList(ctx.annotation(), this::visitAnnotation);
        final SimpleIdentifier name = visitIdentifier(ctx.identifier());
        final Optional<Arguments> arguments = visitNullable(ctx.arguments(), this::visitArguments);
        final Optional<Body<ClassBodyDeclaration>> classBody = visitNullable(ctx.classBody(), this::visitClassBody);

        return new EnumConstant(annotations, name, arguments, classBody);
    }

    @Override
    public Body<ClassBodyDeclaration> visitEnumBodyDeclarations(JavaParser.EnumBodyDeclarationsContext ctx) {
        checkErrorNode("enum body declarations", ctx);

        final List<ClassBodyDeclaration> declarations = visitNullableList(
            ctx.classBodyDeclaration(),
            this::visitClassBodyDeclaration
        );
        return new Body<>(declarations);
    }

    @Override
    public InterfaceDeclaration visitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) {
        checkErrorNode("interface declaration", ctx);

        final SimpleIdentifier name = visitIdentifier(ctx.identifier());
        final Optional<TypeParameters> typeParameters = visitNullable(ctx.typeParameters(), this::visitTypeParameters);
        final Types extendsTypes = visitNullable(ctx.extendsTypes, this::visitTypeList).orElse(Types.empty());
        final Types permitsTypes = visitNullable(ctx.permitsTypes, this::visitTypeList).orElse(Types.empty());
        final Body<MemberDeclarator<InterfaceMemberDeclaration>> body = visitInterfaceBody(ctx.interfaceBody());

        return new InterfaceDeclaration(
            name,
            getNodes(typeParameters),
            extendsTypes.types(),
            permitsTypes.types(),
            body
        );
    }

    @Override
    public Body<ClassBodyDeclaration> visitClassBody(JavaParser.ClassBodyContext ctx) {
        checkErrorNode("class body", ctx);

        final List<ClassBodyDeclaration> declarations = visitNullableList(
            ctx.classBodyDeclaration(),
            this::visitClassBodyDeclaration
        );
        return new Body<>(declarations);
    }

    @Override
    public Body<MemberDeclarator<InterfaceMemberDeclaration>> visitInterfaceBody(JavaParser.InterfaceBodyContext ctx) {
        checkErrorNode("interface body", ctx);

        final List<MemberDeclarator<InterfaceMemberDeclaration>> declarations = visitNullableList(
            ctx.interfaceBodyDeclaration(), this::visitInterfaceBodyDeclaration
        );
        return new Body<>(declarations);
    }

    @Override
    public ClassBodyDeclaration visitClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
        checkErrorNode("class body declaration", ctx);

        if (ctx.block() == null) {
            final Modifiers modifiers = Modifiers
                .merge(visitNullableList(ctx.modifier(), this::visitModifier).stream());
            final ClassMemberDeclaration declaration = visitMemberDeclaration(ctx.memberDeclaration());
            return new MemberDeclarator<>(modifiers, declaration);
        }
        else {
            final boolean isStatic = ctx.STATIC() != null;
            final Block block = visitBlock(ctx.block());
            return new Block(isStatic, block.members());
        }
    }

    @Override
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public ClassMemberDeclaration visitMemberDeclaration(JavaParser.MemberDeclarationContext ctx) {
        checkErrorNode("member declaration", ctx);

        if (ctx.methodDeclaration() != null) {
            return visitMethodDeclaration(ctx.methodDeclaration());
        }
        else if (ctx.genericMethodDeclaration() != null) {
            return visitGenericMethodDeclaration(ctx.genericMethodDeclaration());
        }
        else if (ctx.fieldDeclaration() != null) {
            return visitFieldDeclaration(ctx.fieldDeclaration());
        }
        else if (ctx.constructorDeclaration() != null) {
            return visitConstructorDeclaration(ctx.constructorDeclaration());
        }
        else if (ctx.genericConstructorDeclaration() != null) {
            return visitGenericConstructorDeclaration(ctx.genericConstructorDeclaration());
        }
        else if (ctx.interfaceDeclaration() != null) {
            return visitInterfaceDeclaration(ctx.interfaceDeclaration());
        }
        else if (ctx.annotationTypeDeclaration() != null) {
            return visitAnnotationTypeDeclaration(ctx.annotationTypeDeclaration());
        }
        else if (ctx.classDeclaration() != null) {
            return visitClassDeclaration(ctx.classDeclaration());
        }
        else if (ctx.enumDeclaration() != null) {
            return visitEnumDeclaration(ctx.enumDeclaration());
        }
        else if (ctx.recordDeclaration() != null) {
            return visitRecordDeclaration(ctx.recordDeclaration());
        }
        else {
            throw new InternalParseException("Unknown member declaration!", ctx.getText());
        }
    }

    @Override
    public MethodDeclaration visitMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        checkErrorNode("method declaration", ctx);

        final List<TypeParameter> typeParameters = Collections.emptyList();
        final Type type = visitTypeTypeOrVoid(ctx.typeTypeOrVoid());
        final SimpleIdentifier name = visitIdentifier(ctx.identifier());
        final Parameters parameters = visitFormalParameters(ctx.formalParameters());
        final Optional<IdentifierList> exceptions = visitNullable(
            ctx.qualifiedNameList(), this::visitQualifiedNameList
        );
        final Optional<Block> body = visitMethodBody(ctx.methodBody()).block();

        return new MethodDeclaration(typeParameters, type, name, parameters, getNodes(exceptions), body);
    }

    @Override
    public OptionalBlock visitMethodBody(JavaParser.MethodBodyContext ctx) {
        checkErrorNode("method body", ctx);

        return new OptionalBlock(visitNullable(ctx.block(), this::visitBlock));
    }

    @Override
    public Type visitTypeTypeOrVoid(JavaParser.TypeTypeOrVoidContext ctx) {
        checkErrorNode("type or void", ctx);

        if (ctx.typeType() == null) {
            return new Type.VoidType();
        }
        else {
            return visitTypeType(ctx.typeType());
        }
    }

    @Override
    public MethodDeclaration visitGenericMethodDeclaration(JavaParser.GenericMethodDeclarationContext ctx) {
        checkErrorNode("generic method declaration", ctx);

        final List<TypeParameter> typeParameters = visitTypeParameters(ctx.typeParameters()).typeParameters();
        final MethodDeclaration method = visitMethodDeclaration(ctx.methodDeclaration());

        return new MethodDeclaration(
            typeParameters, method.type(), method.name(), method.parameters(), method.exceptions(), method.body()
        );
    }

    @Override
    public ConstructorDeclaration visitGenericConstructorDeclaration(
        JavaParser.GenericConstructorDeclarationContext ctx
    ) {
        checkErrorNode("generic constructor declaration", ctx);

        final List<TypeParameter> typeParameters = visitTypeParameters(ctx.typeParameters()).typeParameters();
        final ConstructorDeclaration constructor = visitConstructorDeclaration(ctx.constructorDeclaration());

        return new ConstructorDeclaration(
            typeParameters, constructor.name(), constructor.parameters(), constructor.exceptions(), constructor.body()
        );
    }

    @Override
    public ConstructorDeclaration visitConstructorDeclaration(JavaParser.ConstructorDeclarationContext ctx) {
        checkErrorNode("constructor declaration", ctx);

        final List<TypeParameter> typeParameters = Collections.emptyList();
        final SimpleIdentifier name = visitIdentifier(ctx.identifier());
        final Parameters parameters = visitFormalParameters(ctx.formalParameters());
        final Optional<IdentifierList> exceptions = visitNullable(
            ctx.qualifiedNameList(), this::visitQualifiedNameList
        );
        final Block body = visitBlock(ctx.constructorBody);

        return new ConstructorDeclaration(typeParameters, name, parameters, getNodes(exceptions), body);
    }

    @Override
    public FieldDeclaration visitFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        checkErrorNode("field declaration", ctx);

        final Type type = visitTypeType(ctx.typeType());
        final VariableDeclarations declarations = visitVariableDeclarators(ctx.variableDeclarators());

        return new FieldDeclaration(type, declarations.declarations());
    }

    @Override
    public MemberDeclarator<InterfaceMemberDeclaration> visitInterfaceBodyDeclaration(
        JavaParser.InterfaceBodyDeclarationContext ctx
    ) {
        checkErrorNode("interface body declaration", ctx);

        final Modifiers modifiers = Modifiers.merge(visitNullableList(ctx.modifier(), this::visitModifier).stream());
        final InterfaceMemberDeclarationOrMethod declaration = visitInterfaceMemberDeclaration(
            ctx.interfaceMemberDeclaration()
        );
        if (declaration.method() == null) {
            return new MemberDeclarator<>(modifiers, declaration.member());
        }
        else {
            final InterfaceMethod m = declaration.method();
            final MethodDeclaration method = new MethodDeclaration(
                getNodes(m.typeParameters()), m.type(), m.name(), m.parameters(), getNodes(m.exceptions()), m.body()
            );
            return new MemberDeclarator<>(modifiers.merge(declaration.method().modifiers()), method);
        }
    }

    @Override
    public InterfaceMemberDeclarationOrMethod visitInterfaceMemberDeclaration(
        JavaParser.InterfaceMemberDeclarationContext ctx
    ) {
        checkErrorNode("interface member declaration", ctx);

        InterfaceMemberDeclaration member = null;
        InterfaceMethod method = null;

        if (ctx.constDeclaration() != null) {
            member = visitConstDeclaration(ctx.constDeclaration());
        }
        else if (ctx.interfaceMethodDeclaration() != null) {
            method = visitInterfaceMethodDeclaration(ctx.interfaceMethodDeclaration());
        }
        else if (ctx.genericInterfaceMethodDeclaration() != null) {
            method = visitGenericInterfaceMethodDeclaration(ctx.genericInterfaceMethodDeclaration());
        }
        else if (ctx.interfaceDeclaration() != null) {
            member = visitInterfaceDeclaration(ctx.interfaceDeclaration());
        }
        else if (ctx.annotationTypeDeclaration() != null) {
            member = visitAnnotationTypeDeclaration(ctx.annotationTypeDeclaration());
        }
        else if (ctx.classDeclaration() != null) {
            member = visitClassDeclaration(ctx.classDeclaration());
        }
        else if (ctx.enumDeclaration() != null) {
            member = visitEnumDeclaration(ctx.enumDeclaration());
        }
        else if (ctx.recordDeclaration() != null) {
            member = visitRecordDeclaration(ctx.recordDeclaration());
        }
        else {
            throw new InternalParseException("Unknown interface member declaration!", ctx.getText());
        }

        return new InterfaceMemberDeclarationOrMethod(member, method);
    }

    @Override
    public FieldDeclaration visitConstDeclaration(JavaParser.ConstDeclarationContext ctx) {
        checkErrorNode("const declaration", ctx);

        final Type type = visitTypeType(ctx.typeType());
        final List<VariableDeclaration> variables = visitNullableList(
            ctx.constantDeclarator(),
            this::visitConstantDeclarator
        );
        final VariableDeclarations declarations = new VariableDeclarations(variables);

        return new FieldDeclaration(type, declarations.declarations());
    }

    @Override
    public VariableDeclaration visitConstantDeclarator(JavaParser.ConstantDeclaratorContext ctx) {
        checkErrorNode("constant declarator", ctx);

        final VariableDeclarationIdentifier identifier = visitVariableDeclarationIdentifier(
            ctx.identifier(), ctx.LBRACK().size()
        );
        final Expression initializer = visitVariableInitializer(ctx.variableInitializer());

        return new VariableDeclaration(identifier, Optional.of(initializer));
    }

    @Override
    public InterfaceMethod visitInterfaceMethodDeclaration(
        JavaParser.InterfaceMethodDeclarationContext ctx
    ) {
        checkErrorNode("interface method declaration", ctx);

        final Modifiers modifiers = Modifiers
            .merge(visitNullableList(ctx.interfaceMethodModifier(), this::visitInterfaceMethodModifier).stream());
        final InterfaceMethod declaration = visitInterfaceCommonBodyDeclaration(ctx.interfaceCommonBodyDeclaration());

        return declaration.withModifiers(modifiers);
    }

    @Override
    public Modifiers visitInterfaceMethodModifier(JavaParser.InterfaceMethodModifierContext ctx) {
        checkErrorNode("interface method modifier", ctx);

        if (ctx.annotation() == null) {
            final Modifier m = Modifier.tryFromString(ctx.getText());
            return new Modifiers(Collections.emptyList(), List.of(m));
        }
        else {
            return new Modifiers(List.of(visitAnnotation(ctx.annotation())), Collections.emptyList());
        }
    }

    @Override
    public InterfaceMethod visitGenericInterfaceMethodDeclaration(
        JavaParser.GenericInterfaceMethodDeclarationContext ctx
    ) {
        checkErrorNode("generic interface method", ctx);

        final Modifiers modifiers = Modifiers
            .merge(visitNullableList(ctx.interfaceMethodModifier(), this::visitInterfaceMethodModifier).stream());
        final TypeParameters typeParameters = visitTypeParameters(ctx.typeParameters());
        final InterfaceMethod declaration = visitInterfaceCommonBodyDeclaration(ctx.interfaceCommonBodyDeclaration());

        return declaration.withModifiers(modifiers).withTypeParameters(typeParameters);
    }

    @Override
    public InterfaceMethod visitInterfaceCommonBodyDeclaration(JavaParser.InterfaceCommonBodyDeclarationContext ctx) {
        checkErrorNode("interface method declaration", ctx);

        final List<Annotation> annotations = visitNullableList(ctx.annotation(), this::visitAnnotation);
        final Type type = visitTypeTypeOrVoid(ctx.typeTypeOrVoid());
        final SimpleIdentifier name = visitIdentifier(ctx.identifier());
        final Parameters parameters = visitFormalParameters(ctx.formalParameters());
        final Optional<IdentifierList> exceptions = visitNullable(
            ctx.qualifiedNameList(), this::visitQualifiedNameList
        );
        final Optional<Block> body = visitMethodBody(ctx.methodBody()).block();

        return new InterfaceMethod(
            new Modifiers(annotations, Collections.emptyList()),
            Optional.empty(),
            type,
            name,
            parameters,
            exceptions,
            body
        );
    }

    @Override
    public VariableDeclarations visitVariableDeclarators(JavaParser.VariableDeclaratorsContext ctx) {
        checkErrorNode("variable declarations", ctx);

        final List<VariableDeclaration> declarations = visitNullableList(
            ctx.variableDeclarator(),
            this::visitVariableDeclarator
        );
        return new VariableDeclarations(declarations);
    }

    @Override
    public VariableDeclaration visitVariableDeclarator(JavaParser.VariableDeclaratorContext ctx) {
        checkErrorNode("variable declaration", ctx);

        final VariableDeclarationIdentifier identifier = visitVariableDeclaratorId(ctx.variableDeclaratorId());
        final Optional<Expression> initializer = visitNullable(
            ctx.variableInitializer(), this::visitVariableInitializer
        );

        return new VariableDeclaration(identifier, initializer);
    }

    @Override
    public VariableDeclarationIdentifier visitVariableDeclaratorId(JavaParser.VariableDeclaratorIdContext ctx) {
        checkErrorNode("variable declarator label", ctx);

        return visitVariableDeclarationIdentifier(ctx.identifier(), ctx.LBRACK().size());
    }

    private VariableDeclarationIdentifier visitVariableDeclarationIdentifier(
        final JavaParser.IdentifierContext identifier, int bracketCount
    ) {
        final SimpleIdentifier id = visitIdentifier(identifier);
        if (bracketCount == 0) {
            return id;
        }
        else {
            return new VariableDeclarationIdentifier.ArrayVariableIdentifier(id, bracketCount);
        }
    }

    @Override
    public Expression visitVariableInitializer(JavaParser.VariableInitializerContext ctx) {
        checkErrorNode("variable initializer", ctx);

        if (ctx.arrayInitializer() == null) {
            return visitExpression(ctx.expression());
        }
        else {
            return visitArrayInitializer(ctx.arrayInitializer());
        }
    }

    @Override
    public Expression visitArrayInitializer(JavaParser.ArrayInitializerContext ctx) {
        checkErrorNode("array initializer", ctx);

        final List<Expression> expressions = visitNullableList(
            ctx.variableInitializer(), this::visitVariableInitializer
        );
        return new ArrayInitializer(expressions);
    }

    @Override
    public ClassOrInterfaceType visitClassOrInterfaceType(JavaParser.ClassOrInterfaceTypeContext ctx) {
        checkErrorNode("class or interface type", ctx);

        final List<MaybeGenericIdentifier> identifiers = visitNullableList(
            ctx.genericIdentifier(),
            this::visitGenericIdentifier
        );
        return classTypeFromIdentifiers(identifiers);
    }

    @Override
    public MaybeGenericIdentifier visitGenericIdentifier(JavaParser.GenericIdentifierContext ctx) {
        checkErrorNode("generic label", ctx);

        final SimpleIdentifier identifier = visitIdentifier(ctx.identifier());
        final Optional<TypeArguments> typeArguments = visitNullable(ctx.typeArguments(), this::visitTypeArguments);

        return buildIdentifier(identifier, typeArguments);
    }

    @Override
    public TypeArgument visitTypeArgument(JavaParser.TypeArgumentContext ctx) {
        checkErrorNode("type argument", ctx);

        if (ctx.QUESTION() == null) {
            return visitTypeType(ctx.typeType());
        }
        else {
            return visitWildcardType(ctx);
        }
    }

    private TypeArgument.WildcardType visitWildcardType(final JavaParser.TypeArgumentContext ctx) {
        final List<Annotation> annotations = visitNullableList(ctx.annotation(), this::visitAnnotation);
        final Optional<Pair<TypeArgument.WildcardTypeRestriction, Type>> typeRestriction;

        final boolean hasTypeRestriction = ctx.SUPER() == null && ctx.EXTENDS() == null;
        if (hasTypeRestriction || ctx.typeType() == null) {
            typeRestriction = Optional.empty();
        }
        else {
            TypeArgument.WildcardTypeRestriction restriction;
            if (ctx.SUPER() == null) {
                restriction = TypeArgument.WildcardTypeRestriction.EXTENDS;
            }
            else {
                restriction = TypeArgument.WildcardTypeRestriction.SUPER;
            }

            typeRestriction = Optional.of(Pair.of(restriction, visitTypeType(ctx.typeType())));
        }

        return new TypeArgument.WildcardType(annotations, typeRestriction);
    }

    @Override
    public IdentifierList visitQualifiedNameList(JavaParser.QualifiedNameListContext ctx) {
        checkErrorNode("qualified name list", ctx);

        return new IdentifierList(visitNullableList(ctx.qualifiedName(), this::visitQualifiedName));
    }

    @Override
    public Parameters visitFormalParameters(JavaParser.FormalParametersContext ctx) {
        checkErrorNode("formal parameters", ctx);

        final Optional<Parameter.ReceiverParameter> receiverParameter = visitNullable(
            ctx.receiverParameter(),
            this::visitReceiverParameter
        );
        final var formalParameters = visitNullable(
            ctx.formalParameterList(),
            this::visitFormalParameterList
        );

        final List<Parameter> parameters = new ArrayList<>();
        receiverParameter.ifPresent(parameters::add);
        parameters.addAll(getNodes(formalParameters));

        return new Parameters(Collections.unmodifiableList(parameters));
    }

    @Override
    public Parameter.ReceiverParameter visitReceiverParameter(JavaParser.ReceiverParameterContext ctx) {
        checkErrorNode("receiver parameter", ctx);

        final Type type = visitTypeType(ctx.typeType());
        final List<SimpleIdentifier> identifiers = new ArrayList<>(
            visitNullableList(ctx.identifier(), this::visitIdentifier)
        );
        identifiers.add(SimpleIdentifier.thisIdentifier());

        final Identifier parameterName = buildIdentifier(identifiers);
        return new Parameter.ReceiverParameter(type, parameterName);
    }

    @Override
    public Parameters visitFormalParameterList(JavaParser.FormalParameterListContext ctx) {
        checkErrorNode("formal parameter list", ctx);

        final List<Parameter> parameters = new ArrayList<>(ctx.formalParameter().size());
        parameters.addAll(visitNullableList(ctx.formalParameter(), this::visitFormalParameter));
        visitNullable(ctx.lastFormalParameter(), this::visitLastFormalParameter).ifPresent(parameters::add);

        return new Parameters(Collections.unmodifiableList(parameters));
    }

    @Override
    public Parameter.FormalParameter visitFormalParameter(JavaParser.FormalParameterContext ctx) {
        checkErrorNode("formal parameter", ctx);

        final List<VariableModifier> modifiers = visitNullableList(ctx.variableModifier(), this::visitVariableModifier);
        final Type type = visitTypeType(ctx.typeType());
        final VariableDeclarationIdentifier identifier = visitVariableDeclaratorId(ctx.variableDeclaratorId());

        return new Parameter.FormalParameter(modifiers, type, Collections.emptyList(), false, identifier);
    }

    @Override
    public Parameter.FormalParameter visitLastFormalParameter(JavaParser.LastFormalParameterContext ctx) {
        checkErrorNode("last formal parameter", ctx);

        final List<VariableModifier> modifiers = visitNullableList(ctx.variableModifier(), this::visitVariableModifier);
        final Type type = visitTypeType(ctx.typeType());
        final List<Annotation> annotations = visitNullableList(ctx.annotation(), this::visitAnnotation);
        final boolean isVarargParameter = ctx.ELLIPSIS() != null;
        final VariableDeclarationIdentifier identifier = visitVariableDeclaratorId(ctx.variableDeclaratorId());

        return new Parameter.FormalParameter(modifiers, type, annotations, isVarargParameter, identifier);
    }

    @Override
    public Parameters visitLambdaLVTIList(JavaParser.LambdaLVTIListContext ctx) {
        checkErrorNode("lambda var parameter list", ctx);

        final List<Parameter> parameters = visitNullableList(
            ctx.lambdaLVTIParameter(),
            this::visitLambdaLVTIParameter
        );
        return new Parameters(parameters);
    }

    @Override
    public Parameter.VarParameter visitLambdaLVTIParameter(JavaParser.LambdaLVTIParameterContext ctx) {
        checkErrorNode("lambda var parameter", ctx);

        final List<VariableModifier> modifiers = visitNullableList(ctx.variableModifier(), this::visitVariableModifier);
        return new Parameter.VarParameter(modifiers, visitIdentifier(ctx.identifier()));
    }

    @Override
    public Identifier visitQualifiedName(JavaParser.QualifiedNameContext ctx) {
        checkErrorNode("qualified name", ctx);
        return buildIdentifier(ctx.identifier().stream().map(this::visitIdentifier).toList());
    }

    @Override
    public LiteralExpr<?> visitLiteral(JavaParser.LiteralContext ctx) {
        checkErrorNode("literal", ctx);

        if (ctx.integerLiteral() != null) {
            return visitIntegerLiteral(ctx.integerLiteral());
        }
        else if (ctx.floatLiteral() != null) {
            return visitFloatLiteral(ctx.floatLiteral());
        }
        else if (ctx.CHAR_LITERAL() != null) {
            final char c = ctx.CHAR_LITERAL().getText().charAt(1);
            return new LiteralValueExpr<>(c);
        }
        else if (ctx.STRING_LITERAL() != null) {
            final String str = trim(ctx.STRING_LITERAL().getText(), "\"");
            return new LiteralValueExpr<>(str);
        }
        else if (ctx.BOOL_LITERAL() != null) {
            final boolean value = Boolean.parseBoolean(ctx.BOOL_LITERAL().getText());
            return new LiteralValueExpr<>(value);
        }
        else if (ctx.NULL_LITERAL() != null) {
            return new NullLiteralExpr();
        }
        else if (ctx.TEXT_BLOCK() != null) {
            final String str = trim(ctx.TEXT_BLOCK().getText(), "\"\"\"");
            return new LiteralValueExpr<>(str);
        }
        else {
            throw new UnsupportedOperationException("Unknown literal type!");
        }
    }

    @Override
    public LiteralValueExpr<BigInteger> visitIntegerLiteral(JavaParser.IntegerLiteralContext ctx) {
        checkErrorNode("integer literal", ctx);

        final BigInteger value;

        final String text = normaliseIntLiteral(ctx.getText());
        if (isLongMinValueExpr(text)) {
            value = BigInteger.valueOf(Long.MIN_VALUE);
        }
        else if (ctx.BINARY_LITERAL() != null) {
            // remove 0b from the start
            final String b = text.substring(2);
            value = new BigInteger(b, 2);
        }
        else if (ctx.HEX_LITERAL() != null) {
            final String x = text.substring(2);
            value = new BigInteger(x, 16);
        }
        else if (ctx.OCT_LITERAL() != null) {
            final String o = text.substring(1);
            value = new BigInteger(o, 8);
        }
        else {
            value = new BigInteger(text);
        }

        return new LiteralValueExpr<>(value);
    }

    static boolean isLongMinValueExpr(final String text) {
        final String normalised = normaliseIntLiteral(text);
        return LONG_MIN_VALUE_EXPR_DEC.equals(normalised)
            || LONG_MIN_VALUE_EXPR_HEX.equals(normalised)
            || LONG_MIN_VALUE_EXPR_OCT.equals(normalised)
            || LONG_MIN_VALUE_EXPR_BIN.equals(normalised);
    }

    private static String normaliseIntLiteral(final String text) {
        return text.toLowerCase(Locale.ROOT).replace("_", "").replace("l", "");
    }

    @Override
    public LiteralValueExpr<Double> visitFloatLiteral(JavaParser.FloatLiteralContext ctx) {
        checkErrorNode("float literal", ctx);

        final String value = ctx.getText().toLowerCase(Locale.ROOT).replace("_", "");
        if (value.endsWith("d") || value.endsWith("f")) {
            return new LiteralValueExpr<>(Double.parseDouble(StringUtils.chop(value)));
        }
        else {
            return new LiteralValueExpr<>(Double.parseDouble(value));
        }
    }

    @Override
    public Identifier visitAltAnnotationQualifiedName(JavaParser.AltAnnotationQualifiedNameContext ctx) {
        return annotationAstConversionVisitor.visitAltAnnotationQualifiedName(ctx);
    }

    @Override
    public Annotation visitAnnotation(JavaParser.AnnotationContext ctx) {
        return annotationAstConversionVisitor.visitAnnotation(ctx);
    }

    @Override
    public ElementValuePairs visitElementValuePairs(JavaParser.ElementValuePairsContext ctx) {
        return annotationAstConversionVisitor.visitElementValuePairs(ctx);
    }

    @Override
    public NamedElementValue visitElementValuePair(JavaParser.ElementValuePairContext ctx) {
        return annotationAstConversionVisitor.visitElementValuePair(ctx);
    }

    @Override
    public ElementValue visitElementValue(JavaParser.ElementValueContext ctx) {
        return annotationAstConversionVisitor.visitElementValue(ctx);
    }

    @Override
    public ElementValues visitElementValueArrayInitializer(JavaParser.ElementValueArrayInitializerContext ctx) {
        return annotationAstConversionVisitor.visitElementValueArrayInitializer(ctx);
    }

    @Override
    public AnnotationDeclaration visitAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) {
        return annotationAstConversionVisitor.visitAnnotationTypeDeclaration(ctx);
    }

    @Override
    public Body<MemberDeclarator<AnnotationMemberDeclaration>> visitAnnotationTypeBody(
        JavaParser.AnnotationTypeBodyContext ctx
    ) {
        return annotationAstConversionVisitor.visitAnnotationTypeBody(ctx);
    }

    @Override
    public MemberDeclarator<AnnotationMemberDeclaration> visitAnnotationTypeElementDeclaration(
        JavaParser.AnnotationTypeElementDeclarationContext ctx
    ) {
        return annotationAstConversionVisitor.visitAnnotationTypeElementDeclaration(ctx);
    }

    @Override
    public AnnotationMemberDeclaration visitAnnotationTypeElementRest(JavaParser.AnnotationTypeElementRestContext ctx) {
        return annotationAstConversionVisitor.visitAnnotationTypeElementRest(ctx);
    }

    @Override
    public AnnotationMemberDeclaration visitAnnotationMethodOrConstantRest(
        JavaParser.AnnotationMethodOrConstantRestContext ctx
    ) {
        return annotationAstConversionVisitor.visitAnnotationMethodOrConstantRest(ctx);
    }

    @Override
    public AnnotationMethodDeclaration visitAnnotationMethodRest(JavaParser.AnnotationMethodRestContext ctx) {
        return annotationAstConversionVisitor.visitAnnotationMethodRest(ctx);
    }

    @Override
    public FieldDeclaration visitAnnotationConstantRest(JavaParser.AnnotationConstantRestContext ctx) {
        return annotationAstConversionVisitor.visitAnnotationConstantRest(ctx);
    }

    @Override
    public ElementValue visitDefaultValue(JavaParser.DefaultValueContext ctx) {
        return annotationAstConversionVisitor.visitDefaultValue(ctx);
    }

    @Override
    public AstNode visitModuleDeclaration(JavaParser.ModuleDeclarationContext ctx) {
        checkErrorNode("module declaration", ctx);

        final boolean open = ctx.OPEN() != null;
        final Identifier name = visitQualifiedName(ctx.qualifiedName());
        final ModuleDeclarationBody body = visitModuleBody(ctx.moduleBody());

        return new ModuleDeclaration(open, name, body);
    }

    @Override
    public ModuleDeclarationBody visitModuleBody(JavaParser.ModuleBodyContext ctx) {
        checkErrorNode("module declaration body", ctx);

        final List<ModuleDirective> directives = visitNullableList(
            ctx.moduleDirective(),
            this::visitModuleDirective
        );
        return new ModuleDeclarationBody(directives);
    }

    @Override
    public ModuleDirective visitModuleDirective(JavaParser.ModuleDirectiveContext ctx) {
        checkErrorNode("module directive", ctx);

        final List<Identifier> names = visitNullableList(ctx.qualifiedName(), this::visitQualifiedName);
        final Identifier name = names.get(0);
        final Optional<Identifier> toWithName;
        if (names.size() > 1) {
            toWithName = Optional.of(names.get(1));
        }
        else {
            toWithName = Optional.empty();
        }

        if (ctx.REQUIRES() != null) {
            final var modifiers = visitNullableList(ctx.requiresModifier(), this::visitRequiresModifier);
            return new RequiresModuleDirective(modifiers, name);
        }
        else if (ctx.EXPORTS() != null) {
            return new ExportsModuleDirective(name, toWithName);
        }
        else if (ctx.OPENS() != null) {
            return new OpensModuleDirective(name, toWithName);
        }
        else if (ctx.USES() != null) {
            return new UsesModuleDirective(name);
        }
        else if (ctx.PROVIDES() != null && !ctx.qualifiedNameList().isEmpty()) {
            final IdentifierList qualifiedNameList = visitQualifiedNameList(ctx.qualifiedNameList());
            return new ProvidesModuleDirective(name, qualifiedNameList.names());
        }
        else {
            throw new InternalParseException("Unknown module directive type: " + ctx.getText());
        }
    }

    @Override
    public RequiresModuleDirective.RequiresModifier visitRequiresModifier(
        JavaParser.RequiresModifierContext ctx
    ) {
        return switch (ctx.getText()) {
            case "transitive" -> RequiresModuleDirective.RequiresModifier.TRANSITIVE;
            case "static" -> RequiresModuleDirective.RequiresModifier.STATIC;
            default -> throw new InternalParseException("Unknown module requires modifier: " + ctx.getText());
        };
    }

    @Override
    public RecordDeclaration visitRecordDeclaration(JavaParser.RecordDeclarationContext ctx) {
        checkErrorNode("record declaration", ctx);

        final SimpleIdentifier name = visitIdentifier(ctx.identifier());
        final Optional<TypeParameters> typeParameters = visitNullable(ctx.typeParameters(), this::visitTypeParameters);
        final RecordComponents components = visitRecordHeader(ctx.recordHeader());
        final Types implementsTypes = visitNullable(ctx.typeList(), this::visitTypeList).orElse(Types.empty());
        final Body<ClassBodyDeclaration> body = visitRecordBody(ctx.recordBody());

        return new RecordDeclaration(name, getNodes(typeParameters), components, implementsTypes.types(), body);
    }

    @Override
    public RecordComponents visitRecordHeader(JavaParser.RecordHeaderContext ctx) {
        checkErrorNode("record header", ctx);

        if (ctx.recordComponentList() == null) {
            return new RecordComponents(Collections.emptyList());
        }
        else {
            return visitRecordComponentList(ctx.recordComponentList());
        }
    }

    @Override
    public RecordComponents visitRecordComponentList(JavaParser.RecordComponentListContext ctx) {
        checkErrorNode("record components", ctx);

        final List<RecordComponent> components = visitNullableList(ctx.recordComponent(), this::visitRecordComponent);
        return new RecordComponents(components);
    }

    @Override
    public RecordComponent visitRecordComponent(JavaParser.RecordComponentContext ctx) {
        checkErrorNode("record component", ctx);

        final Type type = visitTypeType(ctx.typeType());
        final SimpleIdentifier name = visitIdentifier(ctx.identifier());

        return new RecordComponent(type, name);
    }

    @Override
    public Body<ClassBodyDeclaration> visitRecordBody(JavaParser.RecordBodyContext ctx) {
        checkErrorNode("record body", ctx);

        final Optional<RecordInitializer> initializer = visitNullable(
            ctx.compactConstructorDeclaration(), this::visitCompactConstructorDeclaration
        );
        final List<ClassBodyDeclaration> declarations = visitNullableList(
            ctx.classBodyDeclaration(),
            this::visitClassBodyDeclaration
        );

        return new Body<>(Stream.concat(initializer.stream(), declarations.stream()).toList());
    }

    @Override
    public RecordInitializer visitCompactConstructorDeclaration(JavaParser.CompactConstructorDeclarationContext ctx) {
        checkErrorNode("record initializer", ctx);

        final Modifiers modifiers = visitModifiers(ctx.classOrInterfaceModifier());
        final Block init = visitBlock(ctx.block());

        return new RecordInitializer(modifiers, init);
    }

    @Override
    public Block visitBlock(JavaParser.BlockContext ctx) {
        checkErrorNode("block", ctx);

        return new Block(false, visitNullableList(ctx.blockStatement(), this::visitBlockStatement));
    }

    @Override
    public Statement visitBlockStatement(JavaParser.BlockStatementContext ctx) {
        return statementAstConversionVisitor.visitBlockStatement(ctx);
    }

    @Override
    public LocalVariableDeclStmt visitLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx) {
        return statementAstConversionVisitor.visitLocalVariableDeclaration(ctx);
    }

    @Override
    public SimpleIdentifier visitIdentifier(JavaParser.IdentifierContext ctx) {
        checkErrorNode("label", ctx);

        final String id = ctx.getText();
        if ("this".equals(id)) {
            return SimpleIdentifier.thisIdentifier();
        }
        else if ("super".equals(id)) {
            return SimpleIdentifier.superIdentifier();
        }
        else {
            return new SimpleIdentifier(id);
        }
    }

    @Override
    public Statement visitLocalTypeDeclaration(JavaParser.LocalTypeDeclarationContext ctx) {
        return statementAstConversionVisitor.visitLocalTypeDeclaration(ctx);
    }

    @Override
    public Statement visitStatement(JavaParser.StatementContext ctx) {
        return statementAstConversionVisitor.visitStatement(ctx);
    }

    @Override
    public AstNode visitYieldStatement(JavaParser.YieldStatementContext ctx) {
        return statementAstConversionVisitor.visitYieldStatement(ctx);
    }

    @Override
    public CatchClause visitCatchClause(JavaParser.CatchClauseContext ctx) {
        return statementAstConversionVisitor.visitCatchClause(ctx);
    }

    @Override
    public Type visitCatchType(JavaParser.CatchTypeContext ctx) {
        return statementAstConversionVisitor.visitCatchType(ctx);
    }

    @Override
    public Block visitFinallyBlock(JavaParser.FinallyBlockContext ctx) {
        return statementAstConversionVisitor.visitFinallyBlock(ctx);
    }

    @Override
    public Resources visitResourceSpecification(JavaParser.ResourceSpecificationContext ctx) {
        return statementAstConversionVisitor.visitResourceSpecification(ctx);
    }

    @Override
    public Resources visitResources(JavaParser.ResourcesContext ctx) {
        return statementAstConversionVisitor.visitResources(ctx);
    }

    @Override
    public Resources.Resource visitResource(JavaParser.ResourceContext ctx) {
        return statementAstConversionVisitor.visitResource(ctx);
    }

    @Override
    public SwitchCase visitSwitchBlockStatementGroup(JavaParser.SwitchBlockStatementGroupContext ctx) {
        return statementAstConversionVisitor.visitSwitchBlockStatementGroup(ctx);
    }

    @Override
    public SwitchLabel visitSwitchLabel(JavaParser.SwitchLabelContext ctx) {
        return statementAstConversionVisitor.visitSwitchLabel(ctx);
    }

    @Override
    public ForStmt.ForControl visitForControl(JavaParser.ForControlContext ctx) {
        return statementAstConversionVisitor.visitForControl(ctx);
    }

    @Override
    public AstNode visitForInit(JavaParser.ForInitContext ctx) {
        return statementAstConversionVisitor.visitForInit(ctx);
    }

    @Override
    public ForStmt.EnhancedFor visitEnhancedForControl(JavaParser.EnhancedForControlContext ctx) {
        return statementAstConversionVisitor.visitEnhancedForControl(ctx);
    }

    @Override
    public Expression visitParExpression(JavaParser.ParExpressionContext ctx) {
        checkErrorNode("parenthesised initializer", ctx);
        return visitExpression(ctx.expression());
    }

    @Override
    public Expressions visitExpressionList(JavaParser.ExpressionListContext ctx) {
        if (ctx == null) {
            return new Expressions(Collections.emptyList());
        }

        checkErrorNode("initializer list", ctx);
        return new Expressions(visitNullableList(ctx.expression(), this::visitExpression));
    }

    @Override
    public InvocationExpression visitMethodCall(JavaParser.MethodCallContext ctx) {
        return expressionAstConversionVisitor.visitMethodCall(ctx);
    }

    @Override
    public Expression visitExpression(JavaParser.ExpressionContext ctx) {
        return expressionAstConversionVisitor.visitExpression(ctx);
    }

    @Override
    public BasePattern visitPattern(JavaParser.PatternContext ctx) {
        checkErrorNode("pattern", ctx);

        return visitBasePattern(ctx.variableModifier(), ctx.typeType(), ctx.annotation(), ctx.identifier());
    }

    @Override
    public LambdaExpr visitLambdaExpression(JavaParser.LambdaExpressionContext ctx) {
        return expressionAstConversionVisitor.visitLambdaExpression(ctx);
    }

    @Override
    public LambdaParameters visitLambdaParameters(JavaParser.LambdaParametersContext ctx) {
        return expressionAstConversionVisitor.visitLambdaParameters(ctx);
    }

    @Override
    public AstNode visitLambdaBody(JavaParser.LambdaBodyContext ctx) {
        return expressionAstConversionVisitor.visitLambdaBody(ctx);
    }

    @Override
    public Expression visitPrimary(JavaParser.PrimaryContext ctx) {
        return expressionAstConversionVisitor.visitPrimary(ctx);
    }

    @Override
    public Switch.SwitchExpr visitSwitchExpression(JavaParser.SwitchExpressionContext ctx) {
        return expressionAstConversionVisitor.visitSwitchExpression(ctx);
    }

    @Override
    public SwitchCase visitSwitchLabeledRule(JavaParser.SwitchLabeledRuleContext ctx) {
        checkErrorNode("switch rule", ctx);

        final Block block = visitSwitchRuleOutcome(ctx.switchRuleOutcome());

        if (ctx.DEFAULT() != null) {
            return new SwitchCase(new DefaultLabel(), block);
        }
        else if (ctx.NULL_LITERAL() != null) {
            return new SwitchCase(new SimpleLabel(new NullLiteralExpr()), block);
        }
        else if (ctx.expressionList() != null) {
            final Expressions labels = visitExpressionList(ctx.expressionList());
            if (labels.expressions().size() == 1) {
                return new SwitchCase(new SimpleLabel(labels.expressions().get(0)), block);
            }
            else {
                return new SwitchCase(new ChoiceLabel(labels.expressions()), block);
            }
        }
        else if (ctx.guardedPattern() != null) {
            final Pattern pattern = visitGuardedPattern(ctx.guardedPattern());
            return new SwitchCase(new PatternLabel(pattern), block);
        }
        else {
            throw new InternalParseException("Unknown switch label type.", ctx.getText());
        }
    }

    @Override
    public Pattern visitGuardedPattern(JavaParser.GuardedPatternContext ctx) {
        checkErrorNode("pattern", ctx);

        if (ctx.parPattern != null) {
            return visitGuardedPattern(ctx.parPattern);
        }
        else if (ctx.identifier() == null) {
            final Pattern left = visitGuardedPattern(ctx.guardedPattern());
            final Expression right = visitExpression(ctx.expression(0));
            return new AndPattern(left, right);
        }
        else {
            final BasePattern guard = visitBasePattern(
                ctx.variableModifier(), ctx.typeType(), ctx.annotation(), ctx.identifier()
            );
            final List<Expression> guardedExprs = visitNullableList(ctx.expression(), this::visitExpression);
            if (guardedExprs.isEmpty()) {
                return guard;
            }
            else {
                final Expressions guarded = new Expressions(guardedExprs);
                return new GuardPattern(guard, guarded.expressions());
            }
        }
    }

    private BasePattern visitBasePattern(
        final List<JavaParser.VariableModifierContext> variableModifier,
        final JavaParser.TypeTypeContext type, final List<JavaParser.AnnotationContext> annotation,
        final JavaParser.IdentifierContext identifier
    ) {
        final List<VariableModifier> modifiers = visitNullableList(variableModifier, this::visitVariableModifier);
        final Type parsedType = visitTypeType(type);
        final List<Annotation> annotations = visitNullableList(annotation, this::visitAnnotation);
        final SimpleIdentifier id = visitIdentifier(identifier);

        return new BasePattern(modifiers, parsedType, annotations, id);
    }

    @Override
    public Block visitSwitchRuleOutcome(JavaParser.SwitchRuleOutcomeContext ctx) {
        checkErrorNode("switch rule outcome", ctx);

        if (ctx.block() == null) {
            final List<Statement> statements = visitNullableList(ctx.blockStatement(), this::visitBlockStatement);
            return new Block(false, statements);
        }
        else {
            return visitBlock(ctx.block());
        }
    }

    @Override
    public ClassType visitClassType(JavaParser.ClassTypeContext ctx) {
        checkErrorNode("class type", ctx);

        final Optional<ClassOrInterfaceType> scope = visitNullable(
            ctx.classOrInterfaceType(),
            this::visitClassOrInterfaceType
        );
        final List<Annotation> annotations = visitNullableList(ctx.annotation(), this::visitAnnotation);
        final SimpleIdentifier identifier = visitIdentifier(ctx.identifier());
        final Optional<TypeArguments> typeArguments = visitNullable(ctx.typeArguments(), this::visitTypeArguments);

        return new ClassType(scope, annotations, identifier, getNodes(typeArguments));
    }

    @Override
    public Expression visitCreator(JavaParser.CreatorContext ctx) {
        return expressionAstConversionVisitor.visitCreator(ctx);
    }

    @Override
    public Type visitCreatedName(JavaParser.CreatedNameContext ctx) {
        return expressionAstConversionVisitor.visitCreatedName(ctx);
    }

    @Override
    public MaybeGenericIdentifier visitCreatedNameIdentifier(JavaParser.CreatedNameIdentifierContext ctx) {
        return expressionAstConversionVisitor.visitCreatedNameIdentifier(ctx);
    }

    @Override
    public ClassCreationExpression visitInnerCreator(JavaParser.InnerCreatorContext ctx) {
        return expressionAstConversionVisitor.visitInnerCreator(ctx);
    }

    @Override
    public ArrayCreation visitArrayCreatorRest(JavaParser.ArrayCreatorRestContext ctx) {
        return expressionAstConversionVisitor.visitArrayCreatorRest(ctx);
    }

    @Override
    public AstNode visitClassCreatorRest(JavaParser.ClassCreatorRestContext ctx) {
        return expressionAstConversionVisitor.visitClassCreatorRest(ctx);
    }

    @Override
    public Expression visitExplicitGenericInvocation(JavaParser.ExplicitGenericInvocationContext ctx) {
        return expressionAstConversionVisitor.visitExplicitGenericInvocation(ctx);
    }

    @Override
    public TypeArguments visitTypeArgumentsOrDiamond(JavaParser.TypeArgumentsOrDiamondContext ctx) {
        checkErrorNode("type arguments or diamond", ctx);

        if (ctx.typeArguments() == null) {
            return new TypeArguments(Collections.emptyList());
        }
        else {
            return visitTypeArguments(ctx.typeArguments());
        }
    }

    @Override
    public TypeArguments visitNonWildcardTypeArgumentsOrDiamond(
        JavaParser.NonWildcardTypeArgumentsOrDiamondContext ctx
    ) {
        checkErrorNode("non wildcard type arguments or diamond", ctx);

        if (ctx.nonWildcardTypeArguments() == null) {
            return new TypeArguments(Collections.emptyList());
        }
        else {
            return visitNonWildcardTypeArguments(ctx.nonWildcardTypeArguments());
        }
    }

    @Override
    public TypeArguments visitNonWildcardTypeArguments(JavaParser.NonWildcardTypeArgumentsContext ctx) {
        checkErrorNode("non wildcard type arguments", ctx);

        final Types types = visitTypeList(ctx.typeList());
        final List<TypeArgument> typeArguments = types.types()
            .stream()
            .map(TypeArgument.class::cast)
            .toList();

        return new TypeArguments(typeArguments);
    }

    @Override
    public Types visitTypeList(JavaParser.TypeListContext ctx) {
        checkErrorNode("type list", ctx);

        return new Types(visitNullableList(ctx.typeType(), this::visitTypeType));
    }

    @Override
    public Type visitTypeType(JavaParser.TypeTypeContext ctx) {
        checkErrorNode("type", ctx);

        final List<Annotation> annotations = visitNullableList(ctx.annotation(), this::visitAnnotation);
        final Type type;
        if (ctx.classOrInterfaceType() == null) {
            type = visitPrimitiveType(ctx.primitiveType());
        }
        else {
            type = visitClassOrInterfaceType(ctx.classOrInterfaceType());
        }
        final List<ComplexType.ArrayType> arrayTypes = visitNullableList(
            ctx.annotatedBrackets(), this::visitAnnotatedBrackets
        );

        if (annotations.isEmpty() && arrayTypes.isEmpty()) {
            return type;
        }
        else {
            return new ComplexType(annotations, type, arrayTypes);
        }
    }

    @Override
    public ComplexType.ArrayType visitAnnotatedBrackets(JavaParser.AnnotatedBracketsContext ctx) {
        checkErrorNode("annotated brackets", ctx);

        return new ComplexType.ArrayType(visitNullableList(ctx.annotation(), this::visitAnnotation));
    }

    @Override
    public PrimitiveType visitPrimitiveType(JavaParser.PrimitiveTypeContext ctx) {
        checkErrorNode("primitive type", ctx);

        return PrimitiveType.tryFromString(ctx.getText());
    }

    @Override
    public TypeArguments visitTypeArguments(JavaParser.TypeArgumentsContext ctx) {
        checkErrorNode("type arguments", ctx);

        return new TypeArguments(visitNullableList(ctx.typeArgument(), this::visitTypeArgument));
    }

    @Override
    public SuperInvocation visitSuperSuffix(JavaParser.SuperSuffixContext ctx) {
        return expressionAstConversionVisitor.visitSuperSuffix(ctx);
    }

    @Override
    public InvocationExpression visitExplicitGenericInvocationSuffix(
        JavaParser.ExplicitGenericInvocationSuffixContext ctx
    ) {
        return expressionAstConversionVisitor.visitExplicitGenericInvocationSuffix(ctx);
    }

    @Override
    public Arguments visitArguments(JavaParser.ArgumentsContext ctx) {
        checkErrorNode("arguments", ctx);

        final Optional<Expressions> arguments = visitNullable(ctx.expressionList(), this::visitExpressionList);
        return new Arguments(getNodes(arguments));
    }

    private String trim(final String str, final String remove) {
        return StringUtils.removeEnd(StringUtils.removeStart(str, remove), remove);
    }
}
