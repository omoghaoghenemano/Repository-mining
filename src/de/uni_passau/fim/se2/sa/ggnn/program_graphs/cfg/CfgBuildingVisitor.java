// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.program_graphs.cfg;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.*;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement.CatchClause;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.statement.try_statement.TryStmt;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.Switch;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.SwitchCase;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.switch_node.labels.DefaultLabel;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.PGNode;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Traverses the abstract syntax tree to build the control flow graph.
 * <p>
 * The visit methods take in the currently trailing edges of the graph and return the new trailing edges.
 */
class CfgBuildingVisitor implements AstVisitorWithDefaults<List<PGNode>, List<PGNode>> {

    private final CfgBuilder builder;

    private final Map<String, PGNode> labelledNodes = new HashMap<>();

    private final List<BreakStmt> unresolvedBreaks = new ArrayList<>();

    private final List<ContinueStmt> unresolvedContinues = new ArrayList<>();

    private final Map<Optional<String>, List<ThrowStmt>> unresolvedThrows = new HashMap<>();

    CfgBuildingVisitor(final CfgBuilder builder) {
        this.builder = builder;
    }

    List<ThrowStmt> getUnresolvedThrows() {
        return unresolvedThrows.values().stream().flatMap(List::stream).toList();
    }

    @Override
    public List<PGNode> defaultAction(AstNode node, List<PGNode> arg) {
        List<PGNode> terminalNodes = arg;

        for (final AstNode child : node.children()) {
            terminalNodes = child.accept(this, terminalNodes);
        }

        return terminalNodes;
    }

    // region statements

    @Override
    public List<PGNode> visit(LocalVariableDeclStmt.LocalVarVariableDecl node, List<PGNode> arg) {
        return visitStmt(node, arg);
    }

    @Override
    public List<PGNode> visit(LocalVariableDeclStmt.LocalTypedVariableDecl node, List<PGNode> arg) {
        return visitStmt(node, arg);
    }

    @Override
    public List<PGNode> visit(ExpressionStmt node, List<PGNode> arg) {
        return visitStmt(node, arg);
    }

    @Override
    public List<PGNode> visit(LocalTypeDeclarationStmt node, List<PGNode> arg) {
        // nothing is executed on the declaration of an inner class
        return arg;
    }

    @Override
    public List<PGNode> visit(EmptyStmt node, List<PGNode> arg) {
        return visitStmt(node, arg);
    }

    @Override
    public List<PGNode> visit(ReturnStmt node, List<PGNode> arg) {
        final List<PGNode> returnNode = visitStmt(node, arg);
        returnNode.forEach(builder::addEdgeToExit);
        return Collections.emptyList();
    }

    @Override
    public List<PGNode> visit(AssertStmt node, List<PGNode> arg) {
        final List<PGNode> assertNode = visitStmt(node, arg);
        assertNode.forEach(builder::addEdgeToExit);
        return assertNode;
    }

    @Override
    public List<PGNode> visit(LabelledStmt node, List<PGNode> arg) {
        final PGNode pgNode = getLabelledStatement(node);
        labelledNodes.put(node.label().toString(), pgNode);
        final List<PGNode> terminals = node.statement().accept(this, arg);

        if (node.statement() instanceof Block) {
            return finaliseLabelledBlock(node.label(), terminals);
        }
        else {
            return terminals;
        }
    }

    /**
     * Get the statement that is labelled.
     * <p>
     * For do-while loops the first statement in the loop body is the target, otherwise the statement itself.
     *
     * @param label The labelled statement declaration.
     * @return The CFGNode that is the target of jumps to the label.
     */
    private PGNode getLabelledStatement(final LabelledStmt label) {
        if (label.statement() instanceof LabelledStmt inner) {
            return getLabelledStatement(inner);
        }
        else if (label.statement() instanceof DoWhileStmt doWhileStmt && !doWhileStmt.block().nodes().isEmpty()) {
            return new PGNode(doWhileStmt.block().nodes().get(0));
        }
        else {
            return new PGNode(label.statement());
        }
    }

    private List<PGNode> finaliseLabelledBlock(final Identifier label, final List<PGNode> terminals) {
        final List<BreakStmt> resolvableBreaks = unresolvedBreaks.stream()
            .filter(b -> b.identifier().map(label::equals).orElse(false))
            .toList();
        unresolvedBreaks.removeAll(resolvableBreaks);

        final List<PGNode> resolvedBreaks = resolvableBreaks.stream().map(this::getOrCreateNode).toList();

        return merged(terminals, resolvedBreaks);
    }

    @Override
    public List<PGNode> visit(SynchronizedStmt node, List<PGNode> arg) {
        final List<PGNode> synchronizedNode = visitStmt(node, arg);
        return visit(node.block(), synchronizedNode);
    }

    @Override
    public List<PGNode> visit(BreakStmt stmt, List<PGNode> arg) {
        visitStmt(stmt, arg);
        unresolvedBreaks.add(stmt);
        return Collections.emptyList();
    }

    @Override
    public List<PGNode> visit(ContinueStmt stmt, List<PGNode> arg) {
        final List<PGNode> node = visitStmt(stmt, arg);

        stmt.identifier().ifPresentOrElse(id -> {
            // fetch node from graph again to avoid duplicate node with same content
            final PGNode targetNode = builder.getNode(labelledNodes.get(id.toString()));
            addEdges(node, List.of(targetNode));
        }, () -> unresolvedContinues.add(stmt));

        return Collections.emptyList();
    }

    @Override
    public List<PGNode> visit(IfStmt ifStmt, List<PGNode> arg) {
        final List<PGNode> ifStmtNode = visitStmt(ifStmt, arg);
        final List<PGNode> thenBlockNodes = ifStmt.thenStmt().accept(this, ifStmtNode);
        final List<PGNode> elseBlockNodes = ifStmt.elseStmt()
            .map(elseBlock -> elseBlock.accept(this, ifStmtNode))
            .orElse(Collections.emptyList());

        if (ifStmt.elseStmt().isPresent()) {
            return merged(thenBlockNodes, elseBlockNodes, resolveLabelledBreaks(ifStmtNode));
        }
        else {
            return merged(ifStmtNode, thenBlockNodes, resolveLabelledBreaks(ifStmtNode));
        }
    }

    @Override
    public List<PGNode> visit(Switch.SwitchStmt node, List<PGNode> arg) {
        final List<PGNode> switchHead = visitStmt(node, arg);

        final List<PGNode> breaksToSwitchEnd = new ArrayList<>();
        List<PGNode> trailingNodes = switchHead;
        Optional<PGNode> previousCase = Optional.empty();
        boolean hasDefault = false;

        for (final SwitchCase switchCase : node.cases()) {
            if (switchCase.label() instanceof DefaultLabel) {
                hasDefault = true;
            }

            final PGNode caseNode = new PGNode(switchCase);
            trailingNodes.forEach(n -> builder.addEdge(n, caseNode));

            trailingNodes = switchCase.block().accept(this, List.of(caseNode));
            breaksToSwitchEnd.addAll(resolveBreaks(trailingNodes));

            previousCase.ifPresent(prev -> builder.addEdge(prev, caseNode));
            previousCase = Optional.of(caseNode);
        }

        // all switch cases return, but the switch is non-exhaustive
        // conforms to Java compiler: e.g. it does not exhaustivity-check that all enum variants are covered, so even if
        // the programmer knows all labels have got a case, the unreachable `default` is still needed -> we can check
        // for that here
        if (trailingNodes.isEmpty() && !hasDefault) {
            trailingNodes = switchHead;
        }

        return merged(breaksToSwitchEnd, trailingNodes);
    }

    @Override
    public List<PGNode> visit(ForStmt node, List<PGNode> arg) {
        final List<PGNode> forLoopNodes = visitStmt(node, arg);
        final List<PGNode> lastInnerNodes = node.statementBlock().accept(this, forLoopNodes);
        addEdges(lastInnerNodes, forLoopNodes);

        return finaliseLoop(forLoopNodes);
    }

    @Override
    public List<PGNode> visit(DoWhileStmt node, List<PGNode> arg) {
        final Optional<Statement> firstStmt = node.block().members().stream().findFirst();
        // the nodes of the first statement in the do block
        final List<PGNode> trailingNodes = firstStmt.map(first -> visitStmt(first, arg)).orElse(arg);

        final List<Statement> blockStmts = node.block().members().stream().skip(1).toList();
        List<PGNode> blockTerminalNodes = trailingNodes;
        for (final Statement stmt : blockStmts) {
            blockTerminalNodes = stmt.accept(this, blockTerminalNodes);
        }

        final List<PGNode> doWhileNodes = visitStmt(node, blockTerminalNodes);
        if (firstStmt.isPresent()) {
            addEdges(doWhileNodes, trailingNodes);
        }
        else {
            addEdges(doWhileNodes, doWhileNodes);
        }

        return finaliseLoop(doWhileNodes);
    }

    @Override
    public List<PGNode> visit(WhileStmt node, List<PGNode> arg) {
        final List<PGNode> whileNode = visitStmt(node, arg);
        final List<PGNode> innerNodes = node.block().accept(this, whileNode);
        addEdges(innerNodes, whileNode);

        return finaliseLoop(whileNode);
    }

    // region try-stmt

    @Override
    public List<PGNode> visit(TryStmt.TryWithResources node, List<PGNode> arg) {
        return visitTryStmt(node, arg);
    }

    @Override
    public List<PGNode> visit(TryStmt.RegularTry node, List<PGNode> arg) {
        return visitTryStmt(node, arg);
    }

    /**
     * Adds the control flow edges for a try-(catch)-(finally) block.
     * <p>
     * A few approximations to the control flow have to be made as no exact (sub-)typing and throwing information is
     * known from just the AST:
     * <ul>
     * <li>A {@code throw} statement is only connected directly to a {@code catch} block if the exception type is
     * exactly the same. Otherwise it is connected to the exit node of the CFG.</li>
     * <li>Only the end of the {@code try} block is connected to all {@code catch} blocks and the {@code finally} block.
     * I.e., exceptions thrown within method calls or null pointer dereferences cannot be connected to {@code catch}
     * blocks directly.</li>
     * </ul>
     *
     * @param tryStmt   The try block.
     * @param prevNodes The previous nodes of the control flow graph.
     * @return The terminal nodes after the try block.
     */
    private List<PGNode> visitTryStmt(final TryStmt tryStmt, final List<PGNode> prevNodes) {
        final List<PGNode> tryStmtNode = visitStmt(tryStmt, prevNodes);
        final List<PGNode> tryBodyTerminals = tryStmt.block().accept(this, tryStmtNode);

        finaliseTryBlock(tryStmt, tryBodyTerminals);

        final List<PGNode> catchTerminals = visitCatchClauses(tryStmt);
        final List<PGNode> terminals = merged(tryBodyTerminals, catchTerminals);

        return merged(visitFinallyBlock(tryStmt, terminals), resolveLabelledBreaks(tryStmtNode));
    }

    /**
     * Connects the nodes within the try block to caught exceptions.
     * <p>
     * Best effort, as subtypes of exceptions and exceptions thrown e.g., by method calls are unknown. Therefore, only
     * fully resolves connections between explicitly thrown exceptions and corresponding catch blocks.
     * <p>
     * To handle other cases connects the end of the {@code try} block to each {@code catch} block and the
     * {@code finally} block.
     *
     * @param tryStmt          The try block.
     * @param tryBodyTerminals The CFG nodes at the end of the {@code try} block.
     */
    private void finaliseTryBlock(final TryStmt tryStmt, final List<PGNode> tryBodyTerminals) {
        final Map<String, List<PGNode>> catchBlocks = extractCaughtExceptions(tryStmt);
        resolveCaughtExceptions(catchBlocks);

        Stream.concat(tryStmt.catchClauses().stream(), tryStmt.finallyBlock().stream())
            .forEach(catchClause -> {
                final PGNode catchClauseHead = getOrCreateNode(catchClause);
                addEdges(tryBodyTerminals, List.of(catchClauseHead));
            });
    }

    private List<PGNode> visitCatchClauses(final TryStmt tryStmt) {
        return tryStmt.catchClauses()
            .stream()
            .flatMap(catchClause -> {
                final PGNode catchClauseHead = getOrCreateNode(catchClause);
                return catchClause.accept(this, List.of(catchClauseHead)).stream();
            })
            .toList();
    }

    private List<PGNode> visitFinallyBlock(final TryStmt tryStmt, final List<PGNode> terminals) {
        return tryStmt.finallyBlock()
            .map(finallyStmt -> {
                final PGNode finallyHead = getOrCreateNode(finallyStmt);
                addEdges(terminals, List.of(finallyHead));
                return finallyStmt.accept(this, List.of(finallyHead));
            })
            .orElse(terminals);
    }

    private static Map<String, List<PGNode>> extractCaughtExceptions(final TryStmt tryStmt) {
        return tryStmt.catchClauses()
            .stream()
            .flatMap(catchClause -> {
                final PGNode node = new PGNode(catchClause);
                final Set<String> caughtExceptions = UsedExceptionsExtractor.caughtExceptions(catchClause);
                return caughtExceptions.stream().map(ex -> Pair.of(ex, node));
            })
            .collect(Collectors.groupingBy(Pair::a, Collectors.mapping(Pair::b, Collectors.toList())));
    }

    private void resolveCaughtExceptions(final Map<String, List<PGNode>> catchBlocks) {
        catchBlocks.forEach((key, value) -> {
            final Optional<String> exception = Optional.of(key);
            final List<ThrowStmt> unresolvedThrowsForException = unresolvedThrows
                .getOrDefault(exception, Collections.emptyList());
            unresolvedThrowsForException.stream()
                .map(PGNode::new)
                .map(builder::getNode)
                .map(List::of)
                .forEach(sourceNodes -> addEdges(sourceNodes, value));
            unresolvedThrows.remove(exception);
        });
    }

    @Override
    public List<PGNode> visit(CatchClause node, List<PGNode> arg) {
        return node.catchBlock().accept(this, arg);
    }

    @Override
    public List<PGNode> visit(ThrowStmt node, List<PGNode> arg) {
        visitStmt(node, arg);
        final Optional<String> thrownException = UsedExceptionsExtractor.thrownException(node);
        addUnresolvedThrows(node, thrownException);
        return Collections.emptyList();
    }

    private void addUnresolvedThrows(final ThrowStmt stmt, final Optional<String> exception) {
        unresolvedThrows.compute(exception, (k, v) -> {
            if (v == null) {
                v = new ArrayList<>();
            }
            v.add(stmt);
            return v;
        });
    }

    // endregion try-stmt

    private List<PGNode> visitStmt(Statement stmt, List<PGNode> arg) {
        final PGNode node = new PGNode(stmt);
        arg.forEach(n -> builder.addEdge(n, node));
        return List.of(node);
    }

    // endregion statements

    private void addEdges(final List<PGNode> from, final List<PGNode> to) {
        for (final var f : from) {
            for (final var t : to) {
                builder.addEdge(f, t);
            }
        }
    }

    @SafeVarargs
    private List<PGNode> merged(final List<PGNode>... nodeLists) {
        final List<PGNode> result = new ArrayList<>();
        for (final List<PGNode> nodeList : nodeLists) {
            result.addAll(nodeList);
        }
        return Collections.unmodifiableList(result);
    }

    private List<PGNode> finaliseLoop(final List<PGNode> loopHead) {
        resolveContinues(loopHead);
        return merged(loopHead, resolveBreaks(loopHead));
    }

    private List<PGNode> resolveBreaks(final List<PGNode> loopHead) {
        return resolveBreaks(loopHead, unresolvedBreaks);
    }

    private List<PGNode> resolveLabelledBreaks(final List<PGNode> blockHead) {
        final List<BreakStmt> toBeResolved = unresolvedBreaks.stream()
            .filter(breakStmt -> breakStmt.identifier().isPresent()).toList();
        return resolveBreaks(blockHead, toBeResolved);
    }

    private List<PGNode> resolveBreaks(final List<PGNode> blockHead, final List<BreakStmt> toBeResolved) {
        final List<BreakStmt> resolved = findResolvedNodes(toBeResolved, blockHead);
        unresolvedBreaks.removeAll(resolved);
        return resolved.stream().map(this::getOrCreateNode).toList();
    }

    private void resolveContinues(final List<PGNode> loopHead) {
        final List<ContinueStmt> resolved = findResolvedNodes(unresolvedContinues, loopHead);
        unresolvedContinues.removeAll(resolved);

        final List<PGNode> resolvedNodes = resolved.stream().map(this::getOrCreateNode).toList();
        addEdges(resolvedNodes, loopHead);
    }

    private PGNode getOrCreateNode(final AstNode node) {
        final PGNode pgNode = new PGNode(node);
        return builder.findNode(pgNode).orElse(pgNode);
    }

    private <T extends JumpStatement> List<T> findResolvedNodes(
        final List<T> unresolvedNodes, final List<PGNode> loopHead
    ) {
        final List<T> resolved = new ArrayList<>();

        for (final T jumpStmt : unresolvedNodes) {
            jumpStmt.identifier().ifPresentOrElse(id -> {
                // fetch node from graph again to avoid duplicate node with same content
                final Optional<PGNode> targetNode = builder.findNode(labelledNodes.get(id.toString()));
                if (targetNode.map(loopHead::contains).orElse(false)) {
                    resolved.add(jumpStmt);
                }
            }, () -> resolved.add(jumpStmt));
        }

        return resolved;
    }
}
