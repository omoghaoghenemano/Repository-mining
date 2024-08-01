package de.uni_passau.fim.se2.sa.ggnn.repodriller;

import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRepository;
import org.repodriller.scm.SCMRepository;

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
        JavaWriter javaWriter = new JavaWriter(outputDirectory);
        javaWriter.setFileName("output.csv");
        for (String repoPath : repos) {
            SCMRepository repo = GitRepository.singleProject(repoPath);
            new RepositoryMining()
                    .in(repo)
                    .through(Commits.list(commits))
                    .process(new JavaVisitor(), javaWriter)
                    .mine();
        }
    }
}
