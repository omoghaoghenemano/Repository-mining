// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.parser;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.Modifiers;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.annotation.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.Body;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.FieldDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MemberDeclarator;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.annotation_declaration.AnnotationDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.annotation_declaration.AnnotationMemberDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.annotation_declaration.AnnotationMethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.Type;
import de.uni_passau.fim.se2.sa.ggnn.javaparser.JavaParser;
import de.uni_passau.fim.se2.sa.ggnn.javaparser.JavaParserBaseVisitor;

import java.util.List;
import java.util.Optional;

class AntlrAnnotationAstConversionVisitor extends JavaParserBaseVisitor<AstNode> implements AntlrAstConverter {

    private final AntlrAstConversionVisitor conversionVisitor;

    AntlrAnnotationAstConversionVisitor(final AntlrAstConversionVisitor conversionVisitor) {
        this.conversionVisitor = conversionVisitor;
    }

    @Override
    public Identifier visitAltAnnotationQualifiedName(JavaParser.AltAnnotationQualifiedNameContext ctx) {
        checkErrorNode("annotation qualified name", ctx);

        final List<SimpleIdentifier> identifiers = visitNullableList(
            ctx.identifier(), conversionVisitor::visitIdentifier
        );
        return conversionVisitor.buildIdentifier(identifiers);
    }

    @Override
    public Annotation visitAnnotation(JavaParser.AnnotationContext ctx) {
        checkErrorNode("annotation", ctx);

        final Identifier name = visitNullable(ctx.qualifiedName(), conversionVisitor::visitQualifiedName)
            .orElseGet(() -> conversionVisitor.visitAltAnnotationQualifiedName(ctx.altAnnotationQualifiedName()));

        final Optional<AnnotationValue> value;
        if (ctx.elementValuePairs() != null) {
            value = visitNullable(ctx.elementValuePairs(), conversionVisitor::visitElementValuePairs);
        }
        else if (ctx.elementValue() != null) {
            value = visitNullable(ctx.elementValue(), conversionVisitor::visitElementValue);
        }
        else {
            value = Optional.empty();
        }

        return new Annotation(name, value);
    }

    @Override
    public ElementValuePairs visitElementValuePairs(JavaParser.ElementValuePairsContext ctx) {
        checkErrorNode("element value pairs", ctx);

        final List<NamedElementValue> values = visitNullableList(
            ctx.elementValuePair(),
            conversionVisitor::visitElementValuePair
        );
        return new ElementValuePairs(values);
    }

    @Override
    public NamedElementValue visitElementValuePair(JavaParser.ElementValuePairContext ctx) {
        checkErrorNode("element value pair", ctx);

        final SimpleIdentifier name = conversionVisitor.visitIdentifier(ctx.identifier());
        final ElementValue value = conversionVisitor.visitElementValue(ctx.elementValue());

        return new NamedElementValue(name, value);
    }

    @Override
    public ElementValue visitElementValue(JavaParser.ElementValueContext ctx) {
        checkErrorNode("element value", ctx);

        if (ctx.expression() != null) {
            return conversionVisitor.visitExpression(ctx.expression());
        }
        else if (ctx.annotation() != null) {
            return conversionVisitor.visitAnnotation(ctx.annotation());
        }
        else if (ctx.elementValueArrayInitializer() != null) {
            return conversionVisitor.visitElementValueArrayInitializer(ctx.elementValueArrayInitializer());
        }
        else {
            throw new InternalParseException("Unknown annotation value.", ctx.getText());
        }
    }

    @Override
    public ElementValues visitElementValueArrayInitializer(JavaParser.ElementValueArrayInitializerContext ctx) {
        checkErrorNode("element value array initializer", ctx);

        final List<ElementValue> values = visitNullableList(ctx.elementValue(), conversionVisitor::visitElementValue);
        return new ElementValues(values);
    }

    @Override
    public AnnotationDeclaration visitAnnotationTypeDeclaration(JavaParser.AnnotationTypeDeclarationContext ctx) {
        checkErrorNode("annotation type declaration", ctx);

        final SimpleIdentifier name = conversionVisitor.visitIdentifier(ctx.identifier());
        final Body<MemberDeclarator<AnnotationMemberDeclaration>> body = conversionVisitor
            .visitAnnotationTypeBody(ctx.annotationTypeBody());

        return new AnnotationDeclaration(name, body);
    }

    @Override
    public Body<MemberDeclarator<AnnotationMemberDeclaration>> visitAnnotationTypeBody(
        JavaParser.AnnotationTypeBodyContext ctx
    ) {
        checkErrorNode("annotation type body", ctx);

        final List<MemberDeclarator<AnnotationMemberDeclaration>> members = visitNullableList(
            ctx.annotationTypeElementDeclaration(), conversionVisitor::visitAnnotationTypeElementDeclaration
        );
        return new Body<>(members);
    }

    @Override
    public MemberDeclarator<AnnotationMemberDeclaration> visitAnnotationTypeElementDeclaration(
        JavaParser.AnnotationTypeElementDeclarationContext ctx
    ) {
        checkErrorNode("annotation type element declaration", ctx);

        final Modifiers modifiers = Modifiers
            .merge(visitNullableList(ctx.modifier(), conversionVisitor::visitModifier).stream());
        final AnnotationMemberDeclaration declaration = conversionVisitor
            .visitAnnotationTypeElementRest(ctx.annotationTypeElementRest());

        return new MemberDeclarator<>(modifiers, declaration);
    }

    @Override
    public AnnotationMemberDeclaration visitAnnotationTypeElementRest(JavaParser.AnnotationTypeElementRestContext ctx) {
        checkErrorNode("annotation type element rest", ctx);

        if (ctx.typeType() != null) {
            return visitAnnotationMethodOrField(ctx.typeType(), ctx.annotationMethodOrConstantRest());
        }
        else if (ctx.classDeclaration() != null) {
            return conversionVisitor.visitClassDeclaration(ctx.classDeclaration());
        }
        else if (ctx.interfaceDeclaration() != null) {
            return conversionVisitor.visitInterfaceDeclaration(ctx.interfaceDeclaration());
        }
        else if (ctx.enumDeclaration() != null) {
            return conversionVisitor.visitEnumDeclaration(ctx.enumDeclaration());
        }
        else if (ctx.annotationTypeDeclaration() != null) {
            return conversionVisitor.visitAnnotationTypeDeclaration(ctx.annotationTypeDeclaration());
        }
        else if (ctx.recordDeclaration() != null) {
            return conversionVisitor.visitRecordDeclaration(ctx.recordDeclaration());
        }
        else {
            throw new InternalParseException("Unknown annotation member.", ctx.getText());
        }
    }

    private AnnotationMemberDeclaration visitAnnotationMethodOrField(
        final JavaParser.TypeTypeContext typeContext, final JavaParser.AnnotationMethodOrConstantRestContext ctx
    ) {
        final Type type = conversionVisitor.visitTypeType(typeContext);
        final AnnotationMemberDeclaration decl = conversionVisitor.visitAnnotationMethodOrConstantRest(ctx);

        if (decl instanceof AnnotationMethodDeclaration method) {
            return new AnnotationMethodDeclaration(type, method.name(), method.defaultValue());
        }
        else if (decl instanceof FieldDeclaration field) {
            return new FieldDeclaration(type, field.attributes());
        }
        else {
            throw new InternalParseException("Expected annotation method or attribute.");
        }
    }

    @Override
    public AnnotationMemberDeclaration visitAnnotationMethodOrConstantRest(
        JavaParser.AnnotationMethodOrConstantRestContext ctx
    ) {
        checkErrorNode("annotation method or constant rest", ctx);

        if (ctx.annotationMethodRest() != null) {
            return conversionVisitor.visitAnnotationMethodRest(ctx.annotationMethodRest());
        }
        else if (ctx.annotationConstantRest() != null) {
            return conversionVisitor.visitAnnotationConstantRest(ctx.annotationConstantRest());
        }
        else {
            throw new InternalParseException("Unknown annotation member.", ctx.getText());
        }
    }

    @Override
    public AnnotationMethodDeclaration visitAnnotationMethodRest(JavaParser.AnnotationMethodRestContext ctx) {
        checkErrorNode("annotation annotation method rest", ctx);

        final SimpleIdentifier name = conversionVisitor.visitIdentifier(ctx.identifier());
        final Optional<ElementValue> defaultValue = visitNullable(
            ctx.defaultValue(), conversionVisitor::visitDefaultValue
        );

        return new AnnotationMethodDeclaration(new Type.VoidType(), name, defaultValue);
    }

    @Override
    public FieldDeclaration visitAnnotationConstantRest(JavaParser.AnnotationConstantRestContext ctx) {
        checkErrorNode("annotation constant rest", ctx);

        return new FieldDeclaration(
            new Type.VoidType(),
            conversionVisitor.visitVariableDeclarators(ctx.variableDeclarators()).declarations()
        );
    }

    @Override
    public ElementValue visitDefaultValue(JavaParser.DefaultValueContext ctx) {
        checkErrorNode("annotation default value", ctx);

        return conversionVisitor.visitElementValue(ctx.elementValue());
    }
}
