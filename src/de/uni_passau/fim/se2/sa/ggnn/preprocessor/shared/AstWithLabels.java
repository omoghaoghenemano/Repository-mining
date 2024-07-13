// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2
package de.uni_passau.fim.se2.sa.ggnn.preprocessor.shared;


import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;

import java.util.List;

public record AstWithLabels(List<String> newLabels, AstNode astNode) {

}
