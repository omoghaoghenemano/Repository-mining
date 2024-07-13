// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.preprocessor.shared;

import com.google.common.base.Preconditions;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MethodDeclaration;

import java.util.List;

/**
 * Class for extracting MethodDeclarations in the given AST.
 *
 * @param includeAbstractMethods Boolean whether abstract methods should be included or not.
 */
public record MethodsExtractor(boolean includeAbstractMethods)
    implements ProcessingStep<AstNode, List<MethodDeclaration>> {

    /**
     * Extract all MethodDeclarations as a List from the given AST.
     *
     * @param input The given AST.
     *
     * @return Returns all MethodDeclarations of the given AST.
     */
    @Override
    public List<MethodDeclaration> process(AstNode input) {
        Preconditions.checkNotNull(input);

        return input.accept(new MethodsVisitor(includeAbstractMethods), null).toList();
    }
}
