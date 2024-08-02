package de.uni_passau.fim.se2.sa.ggnn.repodriller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.repodriller.domain.Commit;
import org.repodriller.domain.Developer;
import org.repodriller.domain.Modification;
import org.repodriller.domain.ModificationType;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class JavaVisitorTest {

    private JavaVisitor javaVisitor;
    private Path targetDir;

    @BeforeEach
    public void setUp() throws IOException {
        javaVisitor = new JavaVisitor();
        targetDir = Paths.get("output", "testJavaVisitor");

        // Clean up the target directory before each test
        if (Files.exists(targetDir)) {
            Files.walk(targetDir)
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            file.deleteOnExit();
                        }
                    });
        }
        Files.createDirectories(targetDir);
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Clean up the target directory after each test
        if (Files.exists(targetDir)) {
            Files.walk(targetDir)
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            file.deleteOnExit();
                        }
                    });
        }
    }
    @Test
    public void testProcessWritesJavaModificationsWithNewline() throws IOException {
        String diff = "diff --git a/some/path/TestFile.java b/some/path/TestFile.java\n" +
                "index 0000000..1111111 100644\n" +
                "--- a/some/path/TestFile.java\n" +
                "+++ b/some/path/TestFile.java\n" +
                "@@ -0,0 +1 @@\n" +
                "+public class TestFile {}";
        Modification modification = new Modification(
                "some/old/path",
                "some/path/TestFile.java",
                ModificationType.MODIFY,
                diff,
                "public class TestFile {}"
        );

        Developer author = new Developer("Author Name", "author@example.com");
        Developer committer = new Developer("Committer Name", "committer@example.com");
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        List<String> parents = new ArrayList<>();

        Commit commit = new Commit("abc123",
                author,
                committer,
                date,
                date,
                "Test commit",
                parents
        );

        commit.addModification(modification);

        JavaWriter writer = new JavaWriter(targetDir);

        javaVisitor.process(null, commit, writer);

        Path writtenFile = targetDir.resolve("TestFile.java");
        assertTrue(Files.exists(writtenFile), "Expected file not found.");

        String content = new String(Files.readAllBytes(writtenFile));
        assertEquals("public class TestFile {}\n", content, "Expected file content not found.");
    }

    @Test
    public void testProcessWritesJavaModifications() throws IOException {
        String diff = "diff --git a/some/path/TestFile.java b/some/path/TestFile.java\n" +
                "index 0000000..1111111 100644\n" +
                "--- a/some/path/TestFile.java\n" +
                "+++ b/some/path/TestFile.java\n" +
                "@@ -0,0 +1 @@\n" +
                "+public class TestFile {}";
        Modification modification = new Modification(
                "some/old/path",
                "some/path/TestFile.java",
                ModificationType.MODIFY,
                diff,
                "public class TestFile {}"
        );

        Developer author = new Developer("Author Name", "author@example.com");
        Developer committer = new Developer("Committer Name", "committer@example.com");
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        List<String> parents = new ArrayList<>();

        Commit commit = new Commit("abc123",
                author,
                committer,
                date,
                date,
                "Test commit",
                parents
        );

        commit.addModification(modification);

        JavaWriter writer = new JavaWriter(targetDir);

        javaVisitor.process(null, commit, writer);

        Path writtenFile = targetDir.resolve("TestFile.java");
        assertTrue(Files.exists(writtenFile), "Expected file not found.");

        String content = new String(Files.readAllBytes(writtenFile));
        assertEquals("public class TestFile {}\n", content, "Expected file content not found.");
    }

    @Test
    public void testProcessIgnoresNonJavaModifications() throws IOException {
        Modification nonJavaModification = new Modification(
                "some/old/path", "some/path/TestFile.txt", ModificationType.MODIFY, "diff content", "Some content");

        Developer author = new Developer("Author Name", "author@example.com");
        Developer committer = new Developer("Committer Name", "committer@example.com");
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Commit commit = new Commit("abc123", author, committer, date, date, "Test commit", Collections.singletonList(nonJavaModification.toString()));

        JavaWriter writer = new JavaWriter(targetDir);

        javaVisitor.process(null, commit, writer);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir)) {
            assertTrue(!stream.iterator().hasNext(), "No files should be written for non-Java modifications.");
        }
    }

    @Test
    public void testProcessHandlesEmptyModificationsList() throws IOException {
        Developer author = new Developer("Author Name", "author@example.com");
        Developer committer = new Developer("Committer Name", "committer@example.com");
        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Commit commit = new Commit("abc123", author, committer, date, date, "Test commit", Collections.emptyList());

        JavaWriter writer = new JavaWriter(targetDir);

        javaVisitor.process(null, commit, writer);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir)) {
            assertTrue(!stream.iterator().hasNext(), "No files should be written for empty modifications list.");
        }
    }


}
