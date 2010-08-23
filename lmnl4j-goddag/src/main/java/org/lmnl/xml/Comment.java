package org.lmnl.xml;

import org.lmnl.AnnotationNode;
import org.lmnl.AnnotationNodeFactory;
import org.neo4j.graphdb.Node;

public class Comment extends XmlAnnotationNode {
	public static final String NODE_TYPE = "xml:comment";

	public Comment(AnnotationNodeFactory nodeFactory, Node node, AnnotationNode root) {
		super(nodeFactory, node, root);
	}

	public void setComment(String value) {
		getUnderlyingNode().setProperty("comment", value);
	}

	public String getComment() {
		return (String) getUnderlyingNode().getProperty("comment");
	}

	@Override
	public String toString() {
		return "<!-- " + getComment() + " --> [" + getUnderlyingNode() + "]";
	}
	@Override
	public void copyProperties(AnnotationNode other) {
		super.copyProperties(other);
		Comment otherComment = (Comment) other;
		setComment(otherComment.getComment());
	}
}
