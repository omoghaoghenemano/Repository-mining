package de.uni_passau.fim.se2.sa.ggnn.preprocessor.ggnn.visitors;

import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.declaration.MethodDeclaration;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitorWithDefaults;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg.DataFlowFacts;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg.Definition;
import de.uni_passau.fim.se2.sa.ggnn.program_graphs.ddg.Use;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.IdentityWrapper;
import de.uni_passau.fim.se2.sa.ggnn.util.functional.Pair;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Visitor for inferring LAST_WRITE edges.
 */
public class LastWriteVisitor implements
        AstVisitorWithDefaults<Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>>> {

    private final IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap;
    private final DataFlowFacts dataFlowFacts;

    public LastWriteVisitor(IdentityHashMap<AstNode, IdentityWrapper<AstNode>> astNodeMap, DataFlowFacts dataFlowFacts) {
        this.astNodeMap = astNodeMap;
        this.dataFlowFacts = dataFlowFacts;
    }


    @Override
    public Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> defaultAction(AstNode node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> data) {
        return data;
    }

    @Override
    public Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> visit(MethodDeclaration node, Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> data) {
        Set<Pair<IdentityWrapper<AstNode>, IdentityWrapper<AstNode>>> lastWrites = new HashSet<>();

        for (Pair<Definition, Use> pair : dataFlowFacts.getDefUsePairs()) {
            IdentityWrapper<AstNode> defNode = astNodeMap.get(pair.a().def());
            IdentityWrapper<AstNode> useNode = astNodeMap.get(pair.b().use());
            if (defNode != null && useNode != null) {
                lastWrites.add(new Pair<>(defNode, useNode));
            }
        }

        data.addAll(lastWrites);
        return data;
    }


}

    // TODO: Implement required visitors

