package org.lmnl.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.lmnl.AnnotationNode;
import org.lmnl.AnnotationNodeFactory;
import org.neo4j.graphdb.Node;

public class Comment extends XmlAnnotationNode {
	public static final String NODE_TYPE = "xml:comment";

	public Comment(AnnotationNodeFactory nodeFactory, Node node, long owner) {
		super(nodeFactory, node, owner);
	}

	public void setComment(String value) {
		getUnderlyingNode().setProperty("comment", value);
	}

	public String getComment() {
		return (String) getUnderlyingNode().getProperty("comment");
	}

	@Override
	public void exportToStream(XMLStreamWriter destination) throws XMLStreamException {
		destination.writeComment(getComment());
	}

	@Override
	public void copyProperties(AnnotationNode other) {
		super.copyProperties(other);
		Comment otherComment = (Comment) other;
		setComment(otherComment.getComment());
	}
}
