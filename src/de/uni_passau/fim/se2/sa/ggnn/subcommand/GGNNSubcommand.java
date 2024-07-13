// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.subcommand;

import de.uni_passau.fim.se2.sa.ggnn.preprocessor.ProcessingException;
import de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.GGNNPreprocessor;
import de.uni_passau.fim.se2.sa.ggnn.subcommand.mixins.DotGraphCliOptions;
import picocli.CommandLine;

@CommandLine.Command(
        name = "ggnn",
        description = "Builds the ggnn graph."
)
public class GGNNSubcommand extends PreprocessorSubcommand {

    @CommandLine.Mixin
    DotGraphCliOptions dotGraphCliOptions;

    @Override
    protected void process() throws ProcessingException {
        final boolean dotGraph = dotGraphCliOptions.dotGraph;
        final var preprocessor = new GGNNPreprocessor(getCommonOptions(), false, dotGraph);

        preprocessor.process();
    }
}
