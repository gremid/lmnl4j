package org.lmnl.xml;

import org.lmnl.AnnotationNode;
import org.lmnl.AnnotationNodeFactory;
import org.neo4j.graphdb.Node;

public abstract class XmlNamedAnnotationNode extends XmlAnnotationNode {
	
	public XmlNamedAnnotationNode(AnnotationNodeFactory nodeFactory, Node node, AnnotationNode root) {
		super(nodeFactory, node, root);
	}

	public String getNamespace() {
		return (String) getUnderlyingNode().getProperty("ns");
	}

	public void setNamespace(String namespace) {
		getUnderlyingNode().setProperty("ns", namespace);
	}

	public String getName() {
		return (String) getUnderlyingNode().getProperty("name");
	}

	public void setName(String name) {
		getUnderlyingNode().setProperty("name", name);
	}
	
	@Override
	public void copyProperties(AnnotationNode other) {
		super.copyProperties(other);
		XmlNamedAnnotationNode otherAnnotation = (XmlNamedAnnotationNode) other;
		setNamespace(otherAnnotation.getNamespace());
		setName(otherAnnotation.getName());
	}
}
