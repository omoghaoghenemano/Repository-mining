package de.uni_passau.fim.se2.sa.ggnn.repodriller;

import org.repodriller.Study;

import java.nio.file.Path;
import java.util.List;

public class GGNNStudy implements Study {

    private final List<String> repos;
    private final List<String> commits;
    private final Path outputDirectory;

    public GGNNStudy(List<String> repos, List<String> commits, Path outputDirectory) {
        this.repos = repos;
        this.commits = commits;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public void execute() {
        // TODO Implement me
        throw new UnsupportedOperationException("Implement me");
    }
}
