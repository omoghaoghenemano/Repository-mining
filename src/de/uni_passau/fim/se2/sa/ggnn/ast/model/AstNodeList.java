// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model;

import java.util.List;

public interface AstNodeList<I extends AstNode> extends AstNode {

    List<I> nodes();

    @Override
    @SuppressWarnings("unchecked") // cast is safe as type I extends AstNode
    default List<AstNode> children() {
        return (List<AstNode>) nodes();
    }
}
