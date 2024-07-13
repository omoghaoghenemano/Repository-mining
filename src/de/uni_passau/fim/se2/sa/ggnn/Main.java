// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn;

import de.uni_passau.fim.se2.sa.ggnn.subcommand.GGNNSubcommand;
import de.uni_passau.fim.se2.sa.ggnn.subcommand.MineSubcommand;
import picocli.CommandLine;

import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "ggnn",
        mixinStandardHelpOptions = true,
        showDefaultValues = true,
        description = "Preprocessing Java source code into the GGNN graph structure.",
        subcommands = {
                GGNNSubcommand.class,
                MineSubcommand.class
        },
        versionProvider = Main.VersionProvider.class
)
public final class Main implements Callable<Integer> {

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    public static void main(final String[] args) {
        final int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        spec.commandLine().usage(System.out);
        return 0;
    }

    static class VersionProvider implements CommandLine.IVersionProvider {

        @Override
        public String[] getVersion() throws Exception {
            final URL resource = getClass().getResource("/version.txt");
            if (resource == null) {
                return new String[]{"0.0.0"};
            }

            final Properties properties = new Properties();
            try (var input = resource.openStream()) {
                properties.load(input);
                return new String[]{
                        properties.get("ApplicationName") + " " + properties.getProperty("Version")
                };
            }
        }
    }
}
