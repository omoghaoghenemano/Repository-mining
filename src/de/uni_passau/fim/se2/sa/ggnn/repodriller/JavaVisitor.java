package de.uni_passau.fim.se2.sa.ggnn.repodriller;

import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

public class JavaVisitor implements CommitVisitor {

    @Override
    public void process(SCMRepository scmRepository, Commit commit, PersistenceMechanism writer) {

            for (Modification mod : commit.getModifications()) {
                if (mod.fileNameEndsWith(".java")) {
                    // Set the file name in writer
                    String fileName = mod.getNewPath().replace("/", "_");
                    //get only after _underscore
                    String className = extractClassName(fileName);

                    // Set the file name in writer
                    ((JavaWriter) writer).setFileName(className);

                    // Write the source code to the file if it's not empty
                    if (mod.getSourceCode() != null && !mod.getSourceCode().isEmpty()) {
                        writer.write(mod.getSourceCode());
                    }
                }
            }
        }

    private String extractClassName(String fileName) {

        // Split by underscores
        String[] parts = fileName.split("_");

        // Return the last part
        return parts.length > 0 ? parts[parts.length - 1] : fileName;
    }
}
