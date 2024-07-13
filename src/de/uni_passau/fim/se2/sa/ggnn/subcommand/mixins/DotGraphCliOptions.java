// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.subcommand.mixins;

import picocli.CommandLine;

public final class DotGraphCliOptions {

    @CommandLine.Option(
        names = { "-d", "--dotgraph" },
        description = "Generate a dotgraph representation of the graph. Can be converted to an image using the Graphviz tools."
    )
    public boolean dotGraph = false;
}
