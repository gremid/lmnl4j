package org.lmnl.xml;

import java.util.HashMap;
import java.util.Map;

import org.lmnl.AnnotationNode;
import org.lmnl.NodePropertyBasedAnnotationNodeFactory;
import org.neo4j.graphdb.GraphDatabaseService;

public class XmlAnnotationNodeFactory extends NodePropertyBasedAnnotationNodeFactory {
	public static Map<String, Class<? extends AnnotationNode>> XML_NODE_TYPES = new HashMap<String, Class<? extends AnnotationNode>>();

	static {
		XML_NODE_TYPES.put(Element.NODE_TYPE, Element.class);
		XML_NODE_TYPES.put(Attribute.NODE_TYPE, Attribute.class);
		XML_NODE_TYPES.put(Document.NODE_TYPE, Document.class);
		XML_NODE_TYPES.put(Comment.NODE_TYPE, Comment.class);
		XML_NODE_TYPES.put(ProcessingInstruction.NODE_TYPE, ProcessingInstruction.class);
		XML_NODE_TYPES.put(Text.NODE_TYPE, Text.class);
	}

	public XmlAnnotationNodeFactory(GraphDatabaseService db) {
		super(db, XML_NODE_TYPES);
	}
}
