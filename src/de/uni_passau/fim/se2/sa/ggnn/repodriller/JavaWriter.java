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
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            String content;

            if (objects.length > 1) {
                content = (String) objects[2];
                setFileName(((String) objects[1]));
            } else {
                content = (String) objects[0];
            }

            Path filePath = directory.resolve(fileName.replace("/", "_"));
            Files.createDirectories(filePath.getParent());

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
                writer.write(content);
                writer.newLine();
            }

//            writer.flush();
//            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Error writing file: " + e.getMessage(), e);
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
