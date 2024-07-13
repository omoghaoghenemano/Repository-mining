// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.SimpleIdentifier;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;

public class PGNode extends IdentityWrapper<AstNode> {

    /**
     * Entry nodes are labelled with the ‘top’ symbol.
     */
    public static final SimpleIdentifier ENTRY_NODE_LABEL = new SimpleIdentifier("⊤");

    /**
     * Exit nodes are labelled with the ‘bottom’ symbol.
     */
    public static final SimpleIdentifier EXIT_NODE_LABEL = new SimpleIdentifier("⊥");

    public PGNode(AstNode node) {
        super(node);
    }

    public static PGNode entry() {
        return new PGNode(ENTRY_NODE_LABEL);
    }

    public static PGNode exit() {
        return new PGNode(EXIT_NODE_LABEL);
    }

    public AstNode node() {
        return super.elem();
    }

}
