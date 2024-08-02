package de.uni_passau.fim.se2.sa.ggnn.repodriller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.repodriller.RepoDriller;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GGNNStudyTest {
    private GGNNStudy ggnnStudy;
    private Path outputDirectory;

    @BeforeEach
    public void setUp() {
        // Path to the local clone of the JUnit5 repository
        String repoUrl = "https://github.com/HouariZegai/Calculator";
        String commitHash = "2a459a169330e06d93548a7520e9dc6a2c93d1b1"; // example commit hash

        // Use a relative path for the output directory within the project
        outputDirectory = Paths.get("output").toAbsolutePath();

        ggnnStudy = new GGNNStudy(
                Arrays.asList(repoUrl),
                Arrays.asList(commitHash),
                outputDirectory
        );
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Clean up the target directory after each test
        if (Files.exists(outputDirectory)) {
            Files.walk(outputDirectory)
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            file.deleteOnExit();
                        }
                    });
        }
//        System.out.println("Teardown completed. Target directory cleaned: " + outputDirectory.toAbsolutePath());
    }

    @Test
    public void testExecute() {
        new RepoDriller().start(ggnnStudy);

        // Verify that files are created in the output directory
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(outputDirectory)) {
            assertTrue(stream.iterator().hasNext(), "Expected files to be created in the output directory.");
        } catch (IOException e) {
            throw new RuntimeException("Error reading the output directory", e);
        }
    }

    @Test
    public void testOutputContainsExpectedFiles() throws IOException {
        new RepoDriller().start(ggnnStudy);

        // Example file name that should be created (modify according to the actual expected file name)
        Path expectedFile = outputDirectory.resolve("CalculatorUI.java");

        assertTrue(Files.exists(expectedFile), "Expected file not found: " + expectedFile);
    }

    @Test
    public void testOutputNotContainsExpectedFiles() throws IOException {
        new RepoDriller().start(ggnnStudy);

        // Example file name that should be created (modify according to the actual expected file name)
        Path expectedFile = outputDirectory.resolve("Calculator.java");

        assertFalse(Files.exists(expectedFile), "Expected file found: " + expectedFile);
    }

    @Test
    public void testHandlesMultipleCommits() {
        List<String> commitHashes = Arrays.asList(
                "2a459a169330e06d93548a7520e9dc6a2c93d1b1",
                "6a06e71f95b8a08bffacb702a8257113490e52bc" // another example commit hash
        );

        ggnnStudy = new GGNNStudy(
                Arrays.asList("https://github.com/HouariZegai/Calculator"),
                commitHashes,
                outputDirectory
        );

        new RepoDriller().start(ggnnStudy);

        // Verify that files are created for each commit in the output directory
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(outputDirectory)) {
            assertTrue(stream.iterator().hasNext(), "Expected files to be created in the output directory.");
        } catch (IOException e) {
            throw new RuntimeException("Error reading the output directory", e);
        }
    }

    @Test
    public void testHandlesMultipleRepositories() {
        List<String> repoUrls = Arrays.asList(
                "https://github.com/HouariZegai/Calculator",
                "https://github.com/shashirajraja/onlinebookstore" // another example repository URL
        );
        String commitHash = "2a459a169330e06d93548a7520e9dc6a2c93d1b1"; // example commit hash

        ggnnStudy = new GGNNStudy(
                repoUrls,
                Arrays.asList(commitHash, "58c7929bff69672bb1c4f24abc53b887dbc91ec1"),
                outputDirectory
        );

        new RepoDriller().start(ggnnStudy);

        // Verify that files are created for each repository in the output directory
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(outputDirectory)) {
            assertTrue(stream.iterator().hasNext(), "Expected files to be created in the output directory.");
        } catch (IOException e) {
            throw new RuntimeException("Error reading the output directory", e);
        }
    }
}
