package de.uni_passau.fim.se2.sa.ggnn.repodriller;

import org.repodriller.persistence.PersistenceMechanism;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JavaWriter implements PersistenceMechanism {

    private final Path directory;
    private String fileName;
    private FileWriter writer;

    public JavaWriter(Path directory) {
        this.directory = directory;
    }

    @Override
    public synchronized void write(Object... objects) {
        try {
            ensureDirectoryExists();

            String content = extractContent(objects);
            if (objects.length > 1) {
                setFileName((String) objects[1]);
            }

            writeContentToFile(content);

        } catch (IOException e) {
            throw new RuntimeException("Error writing file: " + e.getMessage(), e);
        }
    }

    private void ensureDirectoryExists() throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
    }

    private String extractContent(Object... objects) {
        if (objects.length > 1) {
            return (String) objects[2];
        } else {
            return (String) objects[0];
        }
    }

    private void writeContentToFile(String content) throws IOException {
        Path filePath = directory.resolve(fileName.replace("/", "_"));
        Files.createDirectories(filePath.getParent());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            writer.write(content);
            writer.newLine();
        }
    }


    @Override
    public synchronized void close() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException("Error closing file writer: " + e.getMessage(), e);
            }
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}


