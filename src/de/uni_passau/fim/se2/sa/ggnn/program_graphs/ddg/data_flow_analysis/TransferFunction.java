// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg.data_flow_analysis;

import de.uni_passau.fim.se2.sa.ggnn.program_graphs.PGNode;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg.DataFlowFact;

import java.util.Set;
import java.util.function.BiFunction;

public interface TransferFunction<T extends DataFlowFact> extends BiFunction<PGNode, Set<T>, Set<T>> {
}
