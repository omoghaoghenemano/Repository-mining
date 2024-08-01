package de.uni_passau.fim.se2.sa.ggnn.repodriller;

import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

public class JavaVisitor implements CommitVisitor {

    @Override
    public void process(SCMRepository scmRepository, Commit commit, PersistenceMechanism writer) {
        for(Modification m : commit.getModifications()) {
            writer.write(
                    commit.getHash(),
                    commit.getAuthor().getName(),
                    commit.getCommitter().getName(),
                    m.getFileName(),
                    m.getType()
            );

        }
    }
}
