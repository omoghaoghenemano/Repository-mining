package de.uni_passau.fim.se2.sa.ggnn.repodriller;

import org.repodriller.domain.Commit;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

public class JavaVisitor implements CommitVisitor {

    @Override
    public void process(SCMRepository scmRepository, Commit commit, PersistenceMechanism writer) {
        // TODO Implement me
        throw new UnsupportedOperationException("Implement me");
    }
}
