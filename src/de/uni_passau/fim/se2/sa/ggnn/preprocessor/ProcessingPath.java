// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.preprocessor;

import java.nio.file.Path;
import java.util.Objects;

public final class ProcessingPath {

    private final OutputPathType pathType;
    private final Path path;

    private ProcessingPath(OutputPathType pathType, Path path) {
        this.pathType = pathType;
        this.path = path;
    }

    public static ProcessingPath console() {
        return new ProcessingPath(OutputPathType.CONSOLE, null);
    }

    public static ProcessingPath directory(final Path path) {
        return new ProcessingPath(OutputPathType.DIRECTORY, path);
    }

    public static ProcessingPath file(final Path path) {
        return new ProcessingPath(OutputPathType.FILE, path);
    }

    public Path getPath() {
        return path;
    }

    OutputPathType getPathType() {
        return pathType;
    }

    public boolean isDirectory() {
        return OutputPathType.DIRECTORY.equals(pathType);
    }

    public boolean isConsole() {
        return OutputPathType.CONSOLE.equals(pathType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        else if (o instanceof ProcessingPath that) {
            return pathType == that.pathType && Objects.equals(path, that.path);
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathType, path);
    }

    @Override
    public String toString() {
        return "OutputPath{pathType=" + pathType + ", path=" + path + '}';
    }

    enum OutputPathType {
        CONSOLE,
        DIRECTORY,
        FILE,
    }
}
