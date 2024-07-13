// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.preprocessor;

/**
 * Parameters used by preprocessors.
 *
 * @param inputPath     The directory that contains the Java sources files.
 * @param outputPath    The directory to which the results should be written.
 */
public record CommonPreprocessorOptions(ProcessingPath inputPath, ProcessingPath outputPath) {}
