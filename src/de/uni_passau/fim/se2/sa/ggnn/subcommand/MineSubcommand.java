package de.uni_passau.fim.se2.sa.ggnn.subcommand;

import de.uni_passau.fim.se2.sa.ggnn.repodriller.GGNNStudy;
import org.apache.commons.validator.routines.UrlValidator;
import org.repodriller.RepoDriller;
import picocli.CommandLine.*;
import picocli.CommandLine.Model.CommandSpec;

import java.nio.file.Path;
import java.util.List;

@Command(
        name = "mine",
        description = "Mines repositories for Java methods."
)
public class MineSubcommand implements Runnable {

    @Spec
    CommandSpec spec;

    private List<String> repositories;
    private List<String> commits;
    private Path outputDirectory;


    @Override
    public void run() {
        new RepoDriller().start(new GGNNStudy(repositories, commits, outputDirectory));
    }

    @Option(
            names = {"-r", "--repos"},
            description = {"A List of Repository URLs defining which repositories should be mined."},
            split = ",",
            required = true
    )
    public void setRepositories(final List<String> repositories) {
        UrlValidator urlValidator = new UrlValidator();
        for (String repository : repositories) {
            if (!urlValidator.isValid(repository)) {
                throw new ParameterException(spec.commandLine(), "Invalid repository URL: " + repository);
            }
        }
        this.repositories = repositories;
    }

    @Option(
            names = {"-c", "--commits"},
            description = {"A list of SHA-1 commit ids defining which commits should be mined."},
            split = ",",
            required = true
    )
    public void setCommits(final List<String> commits) {
        for (String commit : commits) {
            if (commit.length() != 40) {
                throw new ParameterException(spec.commandLine(), "Invalid SHA-1 commit length: " + commit);
            }
        }
        this.commits = commits;
    }

    @Option(
            names = {"-o", "--out"},
            description = {"The output directory to which the mined data will be written to."},
            required = true
    )
    public void setOutputDirectory(final Path outputDirectory) {
        if (!outputDirectory.toFile().exists()) {
            throw new ParameterException(spec.commandLine(), "Output directory does not exist.");
        }
        if (!outputDirectory.toFile().isDirectory()) {
            throw new ParameterException(spec.commandLine(), "The defined output path does not correspond to a directory.");
        }
        this.outputDirectory = outputDirectory;
    }
}
