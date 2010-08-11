package org.lmnl;

import org.neo4j.graphdb.Node;

public interface AnnotationNodeFactory {

	boolean supports(Node item);

	<T extends AnnotationNode> T createNode(Class<T> clazz);

	<T extends AnnotationNode> T createNode(Class<T> clazz, AnnotationNode root);	

	AnnotationNode cloneNode(AnnotationNode child);

	AnnotationNode wrapNode(Node node, AnnotationNode root);
}
