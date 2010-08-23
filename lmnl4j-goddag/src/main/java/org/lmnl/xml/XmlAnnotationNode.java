package org.lmnl.xml;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.lmnl.AnnotationNode;
import org.lmnl.AnnotationNodeFactory;
import org.lmnl.xml.xpath.XmlAnnotationNodePointerFactory;
import org.neo4j.graphdb.Node;

public abstract class XmlAnnotationNode extends AnnotationNode {
	static {
		JXPathContextReferenceImpl.addNodePointerFactory(new XmlAnnotationNodePointerFactory());
	}

	public XmlAnnotationNode(AnnotationNodeFactory nodeFactory, Node node, AnnotationNode root) {
		super(nodeFactory, node, root);
	}

	public JXPathContext newXPathContext() {
		return JXPathContext.newContext(this);
	}
	
	public String getTextContent() {
		StringBuilder text = new StringBuilder();
		for (XmlAnnotationNode child : XmlAnnotationNodeFilter.filter(this)) {
			text.append(child.getTextContent());
		}
		return text.toString();
	}
}
