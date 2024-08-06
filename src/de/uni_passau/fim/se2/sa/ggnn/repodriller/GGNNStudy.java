package de.uni_passau.fim.se2.sa.ggnn.repodriller;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.domain.Developer;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.GitRemoteRepository;

import java.io.IOException;
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
        try {
            new RepositoryMining()
                    .in(GitRemoteRepository.allProjectsIn(repos))
                    .through(Commits.list(commits))
                    .process(new JavaVisitor(), new JavaWriter(outputDirectory))
                    .mine();
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException("Error during repository mining: " + e.getMessage(), e);
        }
    }
}