// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.parser;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.ClassBodyDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MemberDeclarator;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.javaparser.JavaParser;

import java.util.Optional;

public class AstCodeParser extends CodeParser {

    private final AntlrAstConversionVisitor converter = new AntlrAstConversionVisitor();

    /**
     * Gets the content of a Java method and parses the code written in java-grammar into the {@link AstNode}
     * representation.
     *
     * @param code The method code to be parsed.
     * @return The parsed {@link AstNode} of the CompilationUnit.
     * @throws ParseException If the Java code was not parseable and an {@link InternalParseException} occurs, then this
     *                        is caught and a checked {@link ParseException} is thrown externally.
     */
    public MemberDeclarator<MethodDeclaration> parseMethod(String code) throws ParseException {
        final JavaParser javaParser = parseCodeFragment(code);
        try {
            final var declaration = converter.visitClassBodyDeclaration(javaParser.classBodyDeclaration());
            return returnMethodElseThrow(declaration);
        }
        catch (InternalParseException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }

    /**
     * Gets the content of a Java method and parses the code written in java-grammar into the {@link AstNode}
     * representation. When an error occurs, an empty optional is returned.
     *
     * @param code The method code to be parsed.
     * @return The parsed {@link AstNode} of the CompilationUnit.
     */
    public Optional<MemberDeclarator<MethodDeclaration>> parseMethodSkipErrors(String code) {
        final JavaParser javaParser = parseCodeFragment(code);

        try {
            final var declaration = converter.visitClassBodyDeclaration(javaParser.classBodyDeclaration());
            return Optional.of(returnMethodElseThrow(declaration));
        }
        catch (ParseException | InternalParseException e) {
            return Optional.empty();
        }

    }

    @SuppressWarnings("unchecked") // safe, cast is checked in isMethodDeclaration
    private MemberDeclarator<MethodDeclaration> returnMethodElseThrow(final ClassBodyDeclaration node)
        throws ParseException {
        if (isMethodDeclaration(node)) {
            return (MemberDeclarator<MethodDeclaration>) node;
        }
        else {
            throw new ParseException("Expected a method declaration.");
        }
    }

    private boolean isMethodDeclaration(final ClassBodyDeclaration declaration) {
        return declaration instanceof MemberDeclarator<?> memberDeclarator
            && memberDeclarator.declaration() instanceof MethodDeclaration;
    }

    /**
     * Gets the content of a Java file and parses the code written in java-grammar into the {@link AstNode}
     * representation.
     *
     * @param code The Java code to be parsed.
     * @return The parsed {@link AstNode} of the CompilationUnit.
     * @throws ParseException If the Java code was not parseable and an {@link InternalParseException} occurs, then this
     *                        is caught and a checked {@link ParseException} is thrown externally.
     */
    public AstNode parseCodeToCompilationUnit(String code) throws ParseException {
        final JavaParser javaParser = parseCodeFragment(code);
        try {
            return converter.visitCompilationUnit(javaParser.compilationUnit());
        }
        catch (InternalParseException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }

}
