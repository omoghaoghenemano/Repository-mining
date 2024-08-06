package de.uni_passau.fim.se2.sa.ggnn.repodriller;

import org.repodriller.persistence.PersistenceMechanism;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class JavaWriter implements PersistenceMechanism {

    private final Path directory;
    private String fileName;
    private BufferedWriter writer;

    public JavaWriter(Path directory) {
        this.directory = directory;
    }

    @Override
    public synchronized void write(Object... objects) {
        if (writer == null) {
            throw new IllegalStateException("Writer not initialized. Call setFileName() before write().");
        }


        try {
            String info;

            if (objects.length > 1) {
                info = (String) objects[2];
                setFileName(((String) objects[1]));
            } else {
                info = (String) objects[0];
            }


                writer.write(info);
                writer.newLine();


            writer.flush();


        } catch (IOException e) {
            throw new RuntimeException("Error writing data", e);
        }
    }



    @Override
    public synchronized void close() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException("Error closing writer", e);
            }
        }
    }

    public void setFileName(String fileName) {
        close();
        this.fileName = fileName;
        initializeWriter();
    }

    private void initializeWriter() {
        try {
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            Path filePath = directory.resolve(fileName);
            writer = Files.newBufferedWriter(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Error initializing writer", e);
        }
    }
}
