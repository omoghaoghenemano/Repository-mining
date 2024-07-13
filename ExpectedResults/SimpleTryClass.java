package fixtures;

import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.FormatterClosedException;

public class SimpleTryClass {
    public static void someMethod(String param) throws FormatterClosedException {
        try (BufferedReader reader = Files.newReader(File.createTempFile("", "txt"), StandardCharsets.UTF_8)) {
            System.out.println(reader.readLine());
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            System.err.println("x");
        }
    }
}
