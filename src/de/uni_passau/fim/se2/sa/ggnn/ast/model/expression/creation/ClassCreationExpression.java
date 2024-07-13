// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.creation;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.expression.InvocationExpression;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.type.TypeArgument;

import java.util.List;

public interface ClassCreationExpression extends InvocationExpression{

    ClassCreationExpression withTypeArguments(List<TypeArgument> typeArguments);
}
