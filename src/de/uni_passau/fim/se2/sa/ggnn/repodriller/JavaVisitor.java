package de.uni_passau.fim.se2.sa.ggnn.repodriller;

import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.RepositoryFile;
import org.repodriller.scm.SCMRepository;

public class JavaVisitor implements CommitVisitor {

    @Override
    public void process(SCMRepository scmRepository, Commit commit, PersistenceMechanism writer) {
        for (Modification modification : commit.getModifications()) {
            if (modification.fileNameEndsWith(".java") || modification.fileNameEndsWith(".javax")) {

                writer.write(
                        commit.getHash(),
                        modification.getFileName(),
                        modification.getSourceCode(),
                        modification.getType(),
                        commit.getAuthor().getName(),
                        commit.getCommitter().getName()
                );
            }
        }
    }
}