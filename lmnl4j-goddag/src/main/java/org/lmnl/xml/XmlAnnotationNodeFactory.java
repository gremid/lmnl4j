package org.lmnl.xml;

import org.lmnl.AnnotationNode;
import org.lmnl.DefaultAnnotationNodeFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

public class XmlAnnotationNodeFactory extends DefaultAnnotationNodeFactory {

	public XmlAnnotationNodeFactory(GraphDatabaseService db) {
		super(db);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AnnotationNode> T createNode(Class<T> clazz, long owner) {
		try {
			return super.createNode(clazz, owner);
		} catch (IllegalArgumentException e) {
		}

		if (Element.class.equals(clazz)) {
			Node node = db.createNode();
			setNodeType(node, Element.NODE_TYPE);
			return (T) new Element(this, node, owner);
		} else if (Attribute.class.equals(clazz)) {
			Node node = db.createNode();
			setNodeType(node, Attribute.NODE_TYPE);
			return (T) new Attribute(this, node, owner);
		} else if (Document.class.equals(clazz)) {
			Node node = db.createNode();
			setNodeType(node, Document.NODE_TYPE);
			return (T) new Document(this, node, owner);
		} else if (Comment.class.equals(clazz)) {
			Node node = db.createNode();
			setNodeType(node, Comment.NODE_TYPE);
			return (T) new Comment(this, node, owner);
		} else if (ProcessingInstruction.class.equals(clazz)) {
			Node node = db.createNode();
			setNodeType(node, ProcessingInstruction.NODE_TYPE);
			return (T) new ProcessingInstruction(this, node, owner);
		} else if (Text.class.equals(clazz)) {
			Node node = db.createNode();
			setNodeType(node, Text.NODE_TYPE);
			return (T) new Text(this, node, owner);
		} else {
			throw new IllegalArgumentException(clazz.toString());
		}
	}

	@Override
	public AnnotationNode wrapNode(Node node, long owner) {
		try {
			return super.wrapNode(node, owner);
		} catch (IllegalArgumentException e) {
		}
		
		String nodeType = getNodeType(node);
		if (Element.NODE_TYPE.equals(nodeType)) {
			return new Element(this, node, owner);
		} else if (Attribute.NODE_TYPE.equals(nodeType)) {
			return new Attribute(this, node, owner);
		} else if (Document.NODE_TYPE.equals(nodeType)) {
			return new Document(this, node, owner);
		} else if (Comment.NODE_TYPE.equals(nodeType)) {
			return new Comment(this, node, owner);
		} else if (ProcessingInstruction.NODE_TYPE.equals(nodeType)) {
			return new ProcessingInstruction(this, node, owner);
		} else if (Text.NODE_TYPE.equals(nodeType)) {
			return new Text(this, node, owner);
		}else {
			throw new IllegalArgumentException(nodeType);
		}
	}
}
