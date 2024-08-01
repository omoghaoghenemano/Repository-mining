package de.uni_passau.fim.se2.sa.ggnn.repodriller;

import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.range.AllCommits;
import org.repodriller.filter.range.CommitRange;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRepository;
import org.repodriller.scm.SCMRepository;

import java.nio.file.Files;
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
        for (String repoUrl : repos) {
            Path localRepoPath = cloneRepository(repoUrl);
            if (localRepoPath != null) {
                try {
                    SCMRepository repo = GitRepository.singleProject(localRepoPath.toString());
                    JavaWriter javaWriter = new JavaWriter(outputDirectory);

                    if (commits != null && commits.size() >= 2) {
                        // Ensure commit range is correctly specified
                        String startCommit = commits.get(0);
                        String endCommit = commits.get(commits.size() - 1);
                        new RepositoryMining()
                                .in(repo)
                                .through(Commits.range(startCommit, endCommit))
                                .process(new JavaVisitor(), javaWriter)
                                .mine();
                    } else { if (commits != null && commits.size() >= 1) {
                        new RepositoryMining()
                                .in(repo)
                                .through(Commits.single(commits.get(0)))
                                .process(new JavaVisitor(), javaWriter)
                                .mine();
                    }

                    }


                } catch (Exception e) {
                    System.err.println("Error processing repository " + localRepoPath + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private Path cloneRepository(String repoUrl) {
        try {
            // Create a directory to clone the repository
            Path tempDir = Files.createTempDirectory("repodriller");
            // Use JGit or a similar library to clone the repository
            org.eclipse.jgit.api.Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(tempDir.toFile())
                    .call();
            return tempDir;
        } catch (Exception e) {
            System.err.println("Failed to clone repository from URL " + repoUrl + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}

