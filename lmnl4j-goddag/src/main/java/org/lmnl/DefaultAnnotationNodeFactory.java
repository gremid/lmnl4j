package org.lmnl;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

public class DefaultAnnotationNodeFactory implements AnnotationNodeFactory {

	protected final GraphDatabaseService db;

	public DefaultAnnotationNodeFactory(GraphDatabaseService db) {
		this.db = db;
	}

	@Override
	public <T extends AnnotationNode> T createNode(Class<T> clazz, long owner) {
		throw new IllegalArgumentException(clazz.toString());
	}

	@Override
	public AnnotationNode cloneNode(AnnotationNode child) {
		AnnotationNode cloned = createNode(child.getClass(), child.getOwner());
		cloned.copyProperties(child);
		return cloned;
	}

	@Override
	public AnnotationNode wrapNode(Node node, long owner) {
		throw new IllegalArgumentException(node.toString());
	}

	public static String getNodeType(Node node) {
		return (String) node.getProperty("nodeType", null);
	}

	public static void setNodeType(Node node, String nodeType) {
		node.setProperty("nodeType", nodeType);
	}
}
