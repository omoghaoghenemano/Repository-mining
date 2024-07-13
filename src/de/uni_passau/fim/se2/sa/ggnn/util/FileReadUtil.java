// SPDX-FileCopyrightText: 2024 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.util;

import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FileReadUtil {

    private static final Logger log = LoggerFactory.getLogger(FileReadUtil.class);

    private FileReadUtil() {
        throw new IllegalCallerException("utility class");
    }

    /**
     * Reads the given file respecting a Unicode byte-order-marker (BOM) if present.
     *
     * @param file Some file.
     * @return The file content.
     * @throws IOException In case reading from the file fails.
     */
    public static String readFileWithBom(final File file) throws IOException {
        return readFileWithBomLines(file).collect(Collectors.joining("\n"));
    }

    /**
     * Reads the given file respecting a Unicode byte-order-marker (BOM) if present.
     *
     * @param file Some file.
     * @return The line-wise file content.
     * @throws IOException In case reading from the file fails.
     */
    public static Stream<String> readFileWithBomLines(final File file) throws IOException {
        final BOMInputStream bis = BOMInputStream.builder().setFile(file).setInclude(false).get();
        final String charset;
        if (bis.hasBOM()) {
            charset = bis.getBOMCharsetName();
        }
        else {
            charset = StandardCharsets.UTF_8.name();
        }

        final BufferedReader buf = new BufferedReader(new InputStreamReader(bis, charset));

        return buf.lines().onClose(() -> {
            try {
                buf.close();
            }
            catch (IOException e) {
                log.debug("File stream '{}' failed to close properly", file, e);
            }
        });
    }
}
