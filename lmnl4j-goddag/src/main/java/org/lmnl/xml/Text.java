package org.lmnl.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.lmnl.AnnotationNode;
import org.lmnl.AnnotationNodeFactory;
import org.neo4j.graphdb.Node;

public class Text extends XmlAnnotationNode {
	public static final String NODE_TYPE = "lmnl:text";

	public Text(AnnotationNodeFactory nodeFactory, Node node, AnnotationNode root) {
		super(nodeFactory, node, root);
	}

	public String getContent() {
		return (String) getUnderlyingNode().getProperty("text");
	}

	public void setContent(String content) {
		getUnderlyingNode().setProperty("text", content);
	}

	@Override
	public void copyProperties(AnnotationNode other) {
		super.copyProperties(other);
		Text otherText = (Text) other;
		setContent(otherText.getContent());
	}

	@Override
	public void exportToStream(XMLStreamWriter destination) throws XMLStreamException {
		destination.writeCharacters(getContent());
	}
	
	@Override
	public String getTextContent() {
		return getContent();
	}
}
