package de.uni_passau.fim.se2.sa.ggnn.repodriller;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

public class JavaWriterTest {

    public static void main(String[] args) {
        Path outputDirectory = Paths.get("output");
        JavaWriter javaWriter = new JavaWriter(outputDirectory);

        // Set the file name and write content
        javaWriter.setFileName("Test.java");
        javaWriter.write("package test;");
        javaWriter.write("public class Test {");
        javaWriter.write("}");
        javaWriter.close();

        // Verify the content of the file
        Path filePath = outputDirectory.resolve("Test.java");
        try {
            String content = Files.readString(filePath);
            String expectedContent = "package test;\npublic class Test {\n}";
            if (content.trim().equals(expectedContent)) {
                System.out.println("File content is correct.");
            } else {
                System.err.println("File content is incorrect.");
                System.err.println("Expected: " + expectedContent);
                System.err.println("Actual: " + content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
