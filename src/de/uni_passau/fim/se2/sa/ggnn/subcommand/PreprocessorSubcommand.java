// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.subcommand;

import de.uni_passau.fim.se2.sa.ggnn.preprocessor.CommonPreprocessorOptions;
import de.uni_passau.fim.se2.sa.ggnn.preprocessor.ProcessingException;
import de.uni_passau.fim.se2.sa.ggnn.preprocessor.ProcessingPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Defines the common commandline parameters for preprocessors.
 * <p>
 * All concrete implementation must have an annotation {@code @CommandLine.Command(name=…)} to enable the usage as a CLI
 * subcommand.
 */
@CommandLine.Command(
        mixinStandardHelpOptions = true,
        showDefaultValues = true
)
public abstract class PreprocessorSubcommand implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(PreprocessorSubcommand.class);

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;

    protected Path sourcePath;

    @CommandLine.ArgGroup(exclusive = false)
    protected Output output;

    /**
     * Sets the input file or directory for the data that should be processed.
     *
     * @param sourcePath An existing file or directory.
     */
    @CommandLine.Option(
            names = {"-s", "--source"},
            description = "A file or directory containing Java source code."
    )
    public void setSourcePath(final File sourcePath) {
        if (sourcePath.exists()) {
            this.sourcePath = sourcePath.toPath();
        } else {
            throw new CommandLine.ParameterException(spec.commandLine(), "The source path must exist.");
        }
    }

    @Override
    public void run() {
        validate();

        try {
            process();
        } catch (ProcessingException e) {
            log.error("Could not process input!", e);
        }
    }

    protected CommonPreprocessorOptions getCommonOptions() {
        return new CommonPreprocessorOptions(getSourcePath(), getOutputPath());
    }

    protected abstract void process() throws ProcessingException;

    private ProcessingPath getSourcePath() {
        if (sourcePath == null) {
            return ProcessingPath.console();
        } else if (sourcePath.toFile().isDirectory()) {
            return ProcessingPath.directory(sourcePath);
        } else {
            return ProcessingPath.file(sourcePath);
        }
    }

    private ProcessingPath getOutputPath() {
        if (output != null && output.outputPath.isPresent()) {
            final Path outputPath = output.outputPath.get();
            return getOutputPath(outputPath);
        } else {
            return ProcessingPath.console();
        }
    }

    private ProcessingPath getOutputPath(final Path path) {
        if (output.outputFile) {
            return ProcessingPath.file(path);
        } else {
            return ProcessingPath.directory(path);
        }
    }

    private void validate() {
        if (output != null && output.outputPath.isPresent()) {
            checkFileOrDirectory(output.outputPath.get(), output.outputFile);
        }
    }

    private void checkFileOrDirectory(final Path outputPath, boolean shouldBeFile) {
        final File outputFile = outputPath.toFile();
        if (!outputFile.exists()) {
            return;
        }

        if (outputFile.isDirectory() && shouldBeFile) {
            throw new CommandLine.ParameterException(
                    spec.commandLine(),
                    "The output file already exists as directory!"
            );
        } else if (!outputFile.isDirectory() && !shouldBeFile) {
            throw new CommandLine.ParameterException(
                    spec.commandLine(),
                    "The output directory already exists as a file!"
            );
        }

    }

    static class Output {

        @CommandLine.Option(
                names = {"-o", "--output"},
                required = true,
                description = "The file or directory which the results should be written to."
        )
        Optional<Path> outputPath;

        @CommandLine.Option(
                names = "-f",
                description = "Indicates that a given output path is a file."
        )
        boolean outputFile = false;
    }
}
