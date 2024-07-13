// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;

public interface MaybeGenericIdentifier extends AstNode {

    SimpleIdentifier identifier();
}
