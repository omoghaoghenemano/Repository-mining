package de.uni_passau.fim.se2.sa.ggnn.repodriller;

import org.repodriller.persistence.PersistenceMechanism;

import java.nio.file.Path;

public class JavaWriter implements PersistenceMechanism {

    private final Path directory;

    public JavaWriter(Path directory) {
        this.directory = directory;
    }

    @Override
    public synchronized void write(Object... objects) {
        // TODO Implement me
        throw new UnsupportedOperationException("Implement me");
    }

    @Override
    public synchronized void close() {
        // TODO Implement me
        throw new UnsupportedOperationException("Implement me");
    }

}
