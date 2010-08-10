package org.lmnl;

import org.neo4j.graphdb.Node;

public interface AnnotationNodeFactory {

	<T extends AnnotationNode> T createNode(Class<T> clazz, long owner);

	AnnotationNode cloneNode(AnnotationNode child);

	AnnotationNode wrapNode(Node node, long owner);
}
