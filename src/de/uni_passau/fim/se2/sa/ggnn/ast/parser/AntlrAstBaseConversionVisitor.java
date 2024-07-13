// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.parser;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNodeList;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.Modifiers;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.VariableDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.interface_declaration.InterfaceMemberDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.Expression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.parameter.Parameters;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.Block;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.javaparser.JavaParserBaseVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class AntlrAstBaseConversionVisitor extends JavaParserBaseVisitor<AstNode> implements AntlrAstConverter {

    protected AntlrAstBaseConversionVisitor() {
        // this class serves as a container for the helper nodes
        // non-abstract with protected constructor, since there are no abstract methods child classes should implement
    }

    protected ClassOrInterfaceType classTypeFromIdentifiers(final List<MaybeGenericIdentifier> identifiers) {
        if (identifiers.isEmpty()) {
            throw new InternalParseException("Missing identifier.");
        }
        else if (identifiers.size() == 1) {
            return classTypeFromIdentifier(identifiers.get(0));
        }

        final boolean generic = identifiers.stream().anyMatch(GenericIdentifier.class::isInstance);
        if (generic) {
            return new QualifiedGenericType(identifiers);
        }
        else {
            final List<SimpleIdentifier> ids = identifiers.stream().map(SimpleIdentifier.class::cast).toList();
            return new QualifiedName(ids);
        }
    }

    private ClassOrInterfaceType classTypeFromIdentifier(final MaybeGenericIdentifier identifier) {
        if (identifier instanceof GenericIdentifier id) {
            return id;
        }
        else if (identifier instanceof SimpleIdentifier id) {
            return id;
        }
        else {
            throw new InternalParseException("Could not interpret '" + identifier + "' as class type.");
        }
    }

    protected Identifier buildIdentifier(final List<SimpleIdentifier> identifiers) {
        if (identifiers.isEmpty()) {
            throw new InternalParseException("An identifier needs to have a name.");
        }
        else if (identifiers.size() == 1) {
            return identifiers.get(0);
        }
        else {
            return new QualifiedName(identifiers);
        }
    }

    protected MaybeGenericIdentifier buildIdentifier(
        final SimpleIdentifier identifier, final Optional<TypeArguments> typeArguments
    ) {
        if (typeArguments.isPresent()) {
            return new GenericIdentifier(identifier, getNodes(typeArguments));
        }
        else {
            return identifier;
        }
    }

    protected interface HelperAstNode extends AstNode {

        @Override
        default <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
            return null;
        }
    }

    protected record OptionalBlock(Optional<Block> block) implements HelperAstNode {

        @Override
        public List<AstNode> children() {
            return new ArrayList<>(block().stream().toList());
        }
    }

    protected record InterfaceMemberDeclarationOrMethod(InterfaceMemberDeclaration member, InterfaceMethod method)
        implements HelperAstNode {

        @Override
        public List<AstNode> children() {
            return List.of(member(), method());
        }
    }

    protected record InterfaceMethod(
        Modifiers modifiers, Optional<TypeParameters> typeParameters, Type type, SimpleIdentifier name,
        Parameters parameters, Optional<IdentifierList> exceptions, Optional<Block> body
    ) implements HelperAstNode {

        InterfaceMethod withModifiers(Modifiers modifiers) {
            return new InterfaceMethod(
                this.modifiers.merge(modifiers), typeParameters, type, name, parameters, exceptions, body
            );
        }

        InterfaceMethod withTypeParameters(TypeParameters typeParameters) {
            return new InterfaceMethod(
                modifiers, Optional.of(typeParameters), type, name, parameters, exceptions, body
            );
        }

        @Override
        public List<AstNode> children() {
            final var children = new ArrayList<AstNode>();
            children.add(modifiers());
            typeParameters().ifPresent(children::add);
            children.addAll(List.of(type(), name(), parameters()));
            exceptions().ifPresent(children::add);
            body().ifPresent(children::add);
            return children;
        }
    }

    protected record IdentifierList(List<Identifier> names) implements AstNodeList<Identifier>, HelperAstNode {

        @Override
        public List<Identifier> nodes() {
            return List.copyOf(names());
        }
    }

    protected record Expressions(List<Expression> expressions) implements AstNodeList<Expression>, HelperAstNode {

        @Override
        public List<Expression> nodes() {
            return List.copyOf(expressions());
        }
    }

    protected record TypeParameters(List<TypeParameter> typeParameters)
        implements AstNodeList<TypeParameter>, HelperAstNode {

        @Override
        public List<TypeParameter> nodes() {
            return List.copyOf(typeParameters());
        }
    }

    protected record VariableDeclarations(List<VariableDeclaration> declarations)
        implements AstNodeList<VariableDeclaration>, HelperAstNode {

        @Override
        public List<VariableDeclaration> nodes() {
            return List.copyOf(declarations());
        }
    }

    protected record TypeArguments(List<TypeArgument> arguments)
        implements AstNodeList<TypeArgument>, HelperAstNode {

        @Override
        public List<TypeArgument> nodes() {
            return List.copyOf(arguments());
        }
    }

    protected record TypeBound(List<Type> bounds) implements AstNodeList<Type>, HelperAstNode {

        @Override
        public List<Type> nodes() {
            return List.copyOf(bounds());
        }
    }

    protected record Types(List<Type> types) implements AstNodeList<Type>, HelperAstNode {

        public static Types empty() {
            return new Types(Collections.emptyList());
        }

        @Override
        public List<Type> nodes() {
            return List.copyOf(types());
        }
    }
}
