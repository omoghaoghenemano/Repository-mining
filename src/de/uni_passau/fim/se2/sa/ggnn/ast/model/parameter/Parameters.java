// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.parameter;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNodeList;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;

import java.util.List;

public record Parameters(List<Parameter> parameters) implements AstNodeList<Parameter>,
    de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.lambda.LambdaParameters {

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public List<Parameter> nodes() {
        return List.copyOf(parameters);
    }
}
