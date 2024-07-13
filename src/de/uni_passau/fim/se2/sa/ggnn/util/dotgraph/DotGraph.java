// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.util.dotgraph;

import com.google.common.collect.Streams;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record DotGraph<A>(
    String title, List<DotGraphEdge<A>> edges, List<DotGraph<A>> subGraphs, Function<A, String> labelGenerator
) {

    private static final int INDENT_SIZE = 2;

    public DotGraph(final String title, final List<DotGraphEdge<A>> edges, List<DotGraph<A>> subGraphs) {
        this(title, edges, subGraphs, n -> n.getClass().getSimpleName());
    }

    public DotGraph(
        final String title, final List<DotGraphEdge<A>> edges, final List<DotGraph<A>> subGraphs,
        final Function<A, String> labelGenerator
    ) {
        this.title = title;
        this.edges = edges;
        this.labelGenerator = labelGenerator;

        final var subGraphTitles = subGraphs.stream().collect(Collectors.groupingBy(DotGraph::title));
        this.subGraphs = subGraphTitles
            .entrySet()
            .stream()
            .flatMap(DotGraph::makeSubgraphTitlesUnique)
            .toList();
    }

    private static <A> Stream<DotGraph<A>> makeSubgraphTitlesUnique(
        final Map.Entry<String, List<DotGraph<A>>> subGraphsByTitle
    ) {
        final int size = subGraphsByTitle.getValue().size();

        if (size <= 1) {
            return subGraphsByTitle.getValue().stream();
        }
        else {
            return Streams.mapWithIndex(subGraphsByTitle.getValue().stream(), (graph, idx) -> {
                final String newTitle = graph.title() + "_" + (idx + 1);
                return new DotGraph<>(newTitle, graph.edges(), graph.subGraphs(), graph.labelGenerator());
            });
        }
    }

    public String build() {
        return build(false, "1", 2);
    }

    private String build(boolean isSubgraph, String indexPrefix, int indentation) {
        final StringBuilder sb = new StringBuilder();
        final String indent = " ".repeat(indentation);
        final Map<A, String> nodeIndices = generateNodeIndices(edges, indexPrefix);

        addHeader(sb, indent, isSubgraph);
        addNodeDefinitions(sb, indent, nodeIndices, labelGenerator);
        addEdges(sb, indent, nodeIndices);
        addSubGraphs(sb, indexPrefix, indentation);

        sb.append(indent, 0, Math.max(0, indent.length() - INDENT_SIZE))
            .append("}\n");

        return sb.toString();
    }

    /**
     * Adds a header to the dotgraph.
     * <p>
     *
     * <pre>
     * digraph "title" {
     *     label="title";
     * </pre>
     *
     * or
     *
     * <pre>
     * subgraph "cluster_title" {
     *     label="title";
     * </pre>
     *
     * @param sb         The graph code to which a header should be added.
     * @param isSubgraph Determines which type of header should be added.
     */
    private void addHeader(final StringBuilder sb, String indent, boolean isSubgraph) {
        sb.append(indent, 0, Math.max(0, indent.length() - INDENT_SIZE));
        if (isSubgraph) {
            sb.append("subgraph \"cluster_");
        }
        else {
            sb.append("digraph \"");
        }
        sb.append(withSafeQuotes(title())).append("\" {\n")
            .append(indent).append("label = \"").append(withSafeQuotes(title()))
            .append("\";\n\n");
    }

    private void addNodeDefinitions(
        final StringBuilder sb, String indent, final Map<A, String> nodes, final Function<A, String> labelGenerator
    ) {
        nodes.forEach((node, index) -> {
            final String nodeLabel = withSafeQuotes(labelGenerator.apply(node));
            sb.append(indent)
                .append(enquoted(index))
                .append(" [label = ")
                .append(enquoted(nodeLabel))
                .append("];\n");
        });
        sb.append('\n');
    }

    private void addEdges(final StringBuilder sb, String indent, final Map<A, String> nodes) {
        for (final DotGraphEdge<A> edge : edges()) {
            final String startIndex = nodes.get(edge.edge().a());
            final String endIndex = nodes.get(edge.edge().b());

            sb.append(indent)
                .append(enquoted(startIndex))
                .append(" -> ")
                .append(enquoted(endIndex));
            edge.colour().ifPresent(colour -> sb.append(" [color = ").append(colour).append(']'));
            sb.append(";\n");
        }
        sb.append('\n');
    }

    private void addSubGraphs(final StringBuilder sb, String indexPrefix, int indentation) {
        for (int i = 0; i < subGraphs.size(); ++i) {
            final DotGraph<A> subGraph = subGraphs.get(i);
            final String newPrefix = indexPrefix + '.' + (i + 1);
            final String dotGraph = subGraph.build(true, newPrefix, indentation + INDENT_SIZE);
            sb.append(dotGraph);
            sb.append('\n');
        }
    }

    private static <A> Map<A, String> generateNodeIndices(final List<DotGraphEdge<A>> edges, String indexPrefix) {
        final List<A> nodes = getUniqueNodes(edges);
        final IdentityHashMap<A, String> result = new IdentityHashMap<>(nodes.size());

        for (int i = 0; i < nodes.size(); ++i) {
            final A node = nodes.get(i);
            final String index = String.format("%s.%d", indexPrefix, i);

            result.put(node, index);
        }

        return result;
    }

    private static <A> List<A> getUniqueNodes(final List<DotGraphEdge<A>> edges) {
        final IdentityHashMap<A, Void> resultSet = new IdentityHashMap<>();

        edges.stream()
            .map(DotGraphEdge::edge)
            .flatMap(p -> Stream.of(p.a(), p.b()))
            .forEach(n -> resultSet.put(n, null));

        return resultSet.keySet().stream().toList();
    }

    private static String withSafeQuotes(final String text) {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"");
    }

    private static String enquoted(String s) {
        return '"' + s + '"';
    }
}
