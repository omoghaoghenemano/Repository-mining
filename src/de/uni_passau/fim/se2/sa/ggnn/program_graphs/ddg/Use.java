// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;

import java.util.Optional;

public record Use(String name, AstNode use, AstNode cfgNode, Optional<AstNode> scope) implements DataFlowFact {

    public Use(String name, AstNode use, AstNode cfgNode) {
        this(name, use, cfgNode, Optional.empty());
    }

    @Override
    public AstNode variable() {
        return use;
    }
}
