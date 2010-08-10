package org.lmnl.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.lmnl.AnnotationNodeFactory;
import org.neo4j.graphdb.Node;

public class Document extends XmlAnnotationNode {
	public static final String NODE_TYPE = "xml:document";

	public Document(AnnotationNodeFactory nodeFactory, Node node, long owner) {
		super(nodeFactory, node, owner);
	}

	@Override
	public void exportToStream(XMLStreamWriter destination) throws XMLStreamException {
		destination.writeStartDocument();
		exportToStream(getChildNodes(), destination);
		destination.writeEndDocument();
	}
}
