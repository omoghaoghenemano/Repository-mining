// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public abstract class PGBuilder<O extends ProgramGraph<O>> implements Builder<MethodDeclaration, O> {

    protected final Set<Pair<PGNode, PGNode>> edges = new HashSet<>();

    protected final PGNode entry = PGNode.entry();

    protected final PGNode exit = PGNode.exit();

    public void addEdge(final PGNode start, final PGNode end) {
        edges.add(Pair.of(start, end));
    }

    public void addEdgeToExit(final PGNode node) {
        edges.add(Pair.of(node, exit));
    }

    public PGNode getNode(final PGNode node) throws NoSuchElementException {
        return findNode(node).orElseThrow();
    }

    public Optional<PGNode> findNode(final PGNode node) {
        return edges.stream()
            .flatMap(edge -> Stream.of(edge.a(), edge.b()))
            .filter(n -> n.equals(node))
            .findFirst();
    }
}
