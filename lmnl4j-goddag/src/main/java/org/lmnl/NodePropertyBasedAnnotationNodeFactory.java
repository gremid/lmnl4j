package org.lmnl;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

public class NodePropertyBasedAnnotationNodeFactory implements AnnotationNodeFactory {
	private static final String NODE_TYPE_PROPERTY = "nodeType";

	protected final GraphDatabaseService db;
	private final Map<String, Class<? extends AnnotationNode>> nodeTypes;
	private final Map<Class<? extends AnnotationNode>, String> nodeTypesReversed;

	public NodePropertyBasedAnnotationNodeFactory(GraphDatabaseService db,
			Map<String, Class<? extends AnnotationNode>> nodeTypes) {
		this.db = db;
		this.nodeTypes = nodeTypes;
		this.nodeTypesReversed = new HashMap<Class<? extends AnnotationNode>, String>(nodeTypes.size());
		for (String nodeType : nodeTypes.keySet()) {
			nodeTypesReversed.put(nodeTypes.get(nodeType), nodeType);
		}
	}

	@Override
	public boolean supports(Node item) {
		return item.hasProperty(NODE_TYPE_PROPERTY) && nodeTypes.keySet().contains(item.getProperty(NODE_TYPE_PROPERTY));
	}

	@Override
	public <T extends AnnotationNode> T createNode(Class<T> clazz) {
		return createNode(clazz, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AnnotationNode> T createNode(Class<T> clazz, AnnotationNode root) {
		if (!nodeTypesReversed.containsKey(clazz)) {
			throw new IllegalArgumentException(clazz.toString());
		}
		Node node = db.createNode();
		setNodeType(node, nodeTypesReversed.get(clazz));
		return (T) wrapNode(node, root);
	}

	@Override
	public AnnotationNode cloneNode(AnnotationNode child) {
		AnnotationNode cloned = createNode(child.getClass(), child.getRoot());
		cloned.copyProperties(child);
		return cloned;
	}

	@Override
	public AnnotationNode wrapNode(Node node, AnnotationNode root) {
		if (!supports(node)) {
			throw new IllegalArgumentException(node.toString());
		}
		try {
			return nodeTypes.get(getNodeType(node))
					.getConstructor(AnnotationNodeFactory.class, Node.class, AnnotationNode.class)
					.newInstance(this, node, root);
		} catch (SecurityException e) {
			throw new AnnotationNodeFactoryException(e);
		} catch (NoSuchMethodException e) {
			throw new AnnotationNodeFactoryException(e);
		} catch (IllegalArgumentException e) {
			throw new AnnotationNodeFactoryException(e);
		} catch (InstantiationException e) {
			throw new AnnotationNodeFactoryException(e);
		} catch (IllegalAccessException e) {
			throw new AnnotationNodeFactoryException(e);
		} catch (InvocationTargetException e) {
			throw new AnnotationNodeFactoryException(e);
		}
	}

	public static String getNodeType(Node node) {
		return (String) node.getProperty(NODE_TYPE_PROPERTY, null);
	}

	public static void setNodeType(Node node, String nodeType) {
		node.setProperty(NODE_TYPE_PROPERTY, nodeType);
	}

	public static class AnnotationNodeFactoryException extends RuntimeException {
		private static final long serialVersionUID = 6061135370969268564L;

		public AnnotationNodeFactoryException(Throwable cause) {
			super(cause);
		}
	}
}
