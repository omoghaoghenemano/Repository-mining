// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.shared;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.CompilationUnit;
import de.uni_passau.fim.se2.sa.ggnn.ast.parser.TransformationException;
import de.uni_passau.fim.se2.sa.ggnn.util.FileReadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

public class JavaFileTransformation<T> {

    private static final Logger log = LoggerFactory.getLogger(JavaFileTransformation.class);

    private static final Pattern JAVA_FILE_PATTERN = Pattern.compile("([a-zA-Z_]\\w*|(module|package)-info)\\.java");

    private final List<Path> files;

    private final boolean printProgress;
    private final DecimalFormat progressFormat;
    private final int total;
    private int counter = 0;

    private final TransformationFunction<String, T> transformationFunction;

    public JavaFileTransformation(
        Path sourceDirectory, boolean printProgress, TransformationFunction<String, T> transformationFunction
    )
        throws IOException {
        files = new ArrayList<>();
        Files.walkFileTree(sourceDirectory, new JavaFileVisitor(files));
        total = files.size();
        this.printProgress = printProgress;
        this.transformationFunction = transformationFunction;

        progressFormat = new DecimalFormat();
        progressFormat.setMaximumFractionDigits(2);
        progressFormat.setMinimumFractionDigits(2);
    }

    /**
     * Parses a java directory and returns the parsed {@link CompilationUnit CompilationUnits} and
     * {@link de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.ModuleDeclaration ModuleDeclarations}.
     * <p>
     * This method is passed the path of a directory. All files of the directory and all transitive files are captured.
     * The listed files are filtered for Java files. I.e., it is assumed that the directory structure follows the usual
     * Java package structure. Any hidden directories or ones that cannot be part of a valid Java package are ignored.
     * <p>
     * If exceptions occur during parsing, the parsing process stops immediately and the corresponding exception is
     * thrown.
     *
     * @return A {@link Stream} of {@link T} containing the parsed Java files.
     * @throws IOException             If paths of the given directory do not match, this exception is thrown.
     * @throws TransformationException If an error occurs during parsing (due to unparseable statements), this exception
     *                                 is thrown.
     */
    public Stream<T> parseDirectory() throws IOException, TransformationException {
        final List<T> parsedCompilationUnits = new ArrayList<>();
        for (final Path path : files) {
            parsedCompilationUnits.add(parseCompilationUnit(path));
        }
        return parsedCompilationUnits.stream();
    }

    /**
     * Parses a java directory and returns the parsed {@link CompilationUnit CompilationUnits} and
     * {@link de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.ModuleDeclaration ModuleDeclarations}.
     * <p>
     * This method is passed the path of a directory. All files of the directory and all transitive files are captured.
     * The listed files are filtered for Java files. I.e., it is assumed that the directory structure follows the usual
     * Java package structure. Any hidden directories or ones that cannot be part of a valid Java package are ignored.
     * <p>
     * If some files cannot be parsed, they are simply logged and skipped.
     *
     * @return A {@link Stream} to {@link T} containing the parsed java files.
     */
    public Stream<T> parseDirectorySkipping() {
        return files.stream().filter(JavaFileTransformation::isJavaFile)
            .map(this::parsePossibleCompilationUnit)
            .flatMap(Optional::stream);
    }

    private Optional<T> parsePossibleCompilationUnit(Path javaFile) {
        try {
            return Optional.of(parseCompilationUnit(javaFile));
        }
        catch (IOException | TransformationException e) {
            log.warn("Could not parse compilation unit: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Parses a single JavaFile.
     * <p>
     * Might return a {@link CompilationUnit} or a
     * {@link de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.ModuleDeclaration}.
     *
     * @param javaFile The path of the Java file.
     * @return The parsed java file in the form of a {@link T} is returned.
     * @throws IOException             Thrown if the Java file path is invalid.
     * @throws TransformationException Thrown if an error occurred while processing the Java file.
     */
    public T parseCompilationUnit(Path javaFile) throws IOException, TransformationException {
        if (!isJavaFile(javaFile)) {
            throw new IOException(String.format("No valid java file path: %s", javaFile));
        }

        if (printProgress) {
            updateStatus(javaFile);
        }

        final String code = FileReadUtil.readFileWithBom(javaFile.toFile());
        try {
            return transformationFunction.apply(code);
        }
        catch (TransformationException e) {
            throw new TransformationException("Could not process file " + javaFile, e);
        }
    }

    private void updateStatus(final Path javaFile) {
        counter++;
        System.err.printf("%s%%: %s\r", progressFormat.format(getPercentageProgress()), javaFile);
    }

    private double getPercentageProgress() {
        return (double) counter / total * 100;
    }

    private static boolean isJavaFile(final Path p) {
        return isValidJavaFileName(p)
            && p.toFile().isFile()
            && "java".equals(com.google.common.io.Files.getFileExtension(p.toString()));
    }

    private static boolean isValidJavaFileName(final Path javaPath) {
        return Optional.ofNullable(javaPath.getFileName())
            .map(Path::toString)
            .map(name -> JAVA_FILE_PATTERN.matcher(name).matches())
            .orElse(false);
    }

    /**
     * Only traverses directories within a Java package directory structure skipping all directories that are not valid
     * Java package names. Collects a list of seen Java sources files.
     */
    private static class JavaFileVisitor extends SimpleFileVisitor<Path> {

        private final List<Path> paths;

        private JavaFileVisitor(List<Path> paths) {
            this.paths = paths;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (isJavaFile(file)) {
                paths.add(file);
            }
            return super.visitFile(file, attrs);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return isHidden(dir) ? SKIP_SUBTREE : CONTINUE;
        }

        private static boolean isHidden(Path dir) {
            return dir.toFile().isHidden();
        }
    }
}
