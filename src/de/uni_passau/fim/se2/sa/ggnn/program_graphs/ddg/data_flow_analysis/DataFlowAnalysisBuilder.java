// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg.data_flow_analysis;

import com.google.common.base.Preconditions;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg.DataFlowFact;

import java.util.Optional;

public class DataFlowAnalysisBuilder<T extends DataFlowFact> {

    private Optional<TransferFunction<T>> transferFunction;

    private Optional<JoinFunction<T>> joinFunction;

    private Optional<FlowDirection<ControlFlowGraph>> flowDirection;

    private final Class<T> factType;

    /**
     * Initialises a new data flow analysis builder.
     *
     * @param factType The type of the data flow facts the analysis is for.
     */
    public DataFlowAnalysisBuilder(final Class<T> factType) {
        this.factType = factType;

        transferFunction = Optional.empty();
        joinFunction = Optional.empty();
        flowDirection = Optional.empty();
    }

    public DataFlowAnalysisBuilder<T> withTransfer(final TransferFunction<T> transferFunction) {
        this.transferFunction = Optional.ofNullable(transferFunction);
        return this;
    }

    public DataFlowAnalysisBuilder<T> withJoin(final JoinFunction<T> joinFunction) {
        this.joinFunction = Optional.ofNullable(joinFunction);
        return this;
    }

    public DataFlowAnalysisBuilder<T> withFlowDirection(final FlowDirection<ControlFlowGraph> flowDirection) {
        this.flowDirection = Optional.ofNullable(flowDirection);
        return this;
    }

    /**
     * Builds the dataflow analysis.
     * <p>
     * {@link #withFlowDirection(FlowDirection)}, etc. must have been called before.
     *
     * @return A data flow analysis as specified before.
     */
    public DataFlowAnalysis<T> build() {
        Preconditions.checkState(transferFunction.isPresent());
        Preconditions.checkState(joinFunction.isPresent());
        Preconditions.checkState(flowDirection.isPresent());

        return new DataFlowAnalysis<>(transferFunction.get(), joinFunction.get(), flowDirection.get(), factType);
    }
}
