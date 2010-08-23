package org.lmnl.xml;

import org.lmnl.AnnotationNode;
import org.lmnl.AnnotationNodeFactory;
import org.neo4j.graphdb.Node;

public class Document extends XmlAnnotationNode {
	public static final String NODE_TYPE = "xml:document";

	public Document(AnnotationNodeFactory nodeFactory, Node node, AnnotationNode root) {
		super(nodeFactory, node, root);
	}
}
