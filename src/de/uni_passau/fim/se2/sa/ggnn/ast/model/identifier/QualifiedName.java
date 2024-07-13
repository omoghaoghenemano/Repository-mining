// SPDX-FileCopyrightText: 2023 Preprocessing Toolbox Contributors
//
// SPDX-License-Identifier: EUPL-1.2

package de.uni_passau.fim.se2.sa.ggnn.ast.model.identifier;

import com.google.common.base.Preconditions;
import de.uni_passau.fim.se2.sa.ggnn.ast.model.AstNode;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.AstVisitor;
import de.uni_passau.fim.se2.sa.ggnn.ast.visitor.Visitable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record QualifiedName(List<SimpleIdentifier> identifiers)
    implements Identifier, Visitable, de.uni_passau.fim.se2.sa.ggnn.ast.model.type.ClassOrInterfaceType {

    public QualifiedName {
        Preconditions.checkArgument(identifiers.size() >= 2, "A qualified name needs to have at least two name parts.");
    }

    public QualifiedName(String id1, String id2, String... identifiers) {
        this(
            Stream
                .concat(Stream.of(id1, id2), Arrays.stream(identifiers))
                .map(SimpleIdentifier::new)
                .toList()
        );
    }

    @Override
    public <R, A> R accept(AstVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public List<AstNode> children() {
        return List.copyOf(identifiers());
    }

    @Override
    public String toString() {
        return identifiers.stream()
            .map(SimpleIdentifier::toString)
            .collect(Collectors.joining("."));
    }
}
