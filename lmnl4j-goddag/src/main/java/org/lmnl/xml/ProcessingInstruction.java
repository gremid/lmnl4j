package org.lmnl.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.lmnl.AnnotationNode;
import org.lmnl.AnnotationNodeFactory;
import org.neo4j.graphdb.Node;

public class ProcessingInstruction extends XmlAnnotationNode {
	public static final String NODE_TYPE = "xml:pi";

	public ProcessingInstruction(AnnotationNodeFactory nodeFactory, Node node, long owner) {
		super(nodeFactory, node, owner);
	}

	public String getTarget() {
		return (String) getUnderlyingNode().getProperty("target");
	}

	public void setTarget(String target) {
		getUnderlyingNode().setProperty("target", target);
	}

	public String getData() {
		return (String) getUnderlyingNode().getProperty("data", null);
	}

	public void setData(String data) {
		if (data == null) {
			getUnderlyingNode().removeProperty("data");
		} else {
			getUnderlyingNode().setProperty("data", data);
		}
	}

	@Override
	public String toString() {
		return "<?" + getTarget() + "> [" + getUnderlyingNode() + "]";
	}
	
	@Override
	public void exportToStream(XMLStreamWriter destination) throws XMLStreamException {
		String piData = getData();
		if (piData == null) {
			destination.writeProcessingInstruction(getTarget());
		} else {
			destination.writeProcessingInstruction(getTarget(), piData);
		}
	}

	@Override
	public void copyProperties(AnnotationNode other) {
		super.copyProperties(other);
		ProcessingInstruction otherPi = (ProcessingInstruction) other;
		setTarget(otherPi.getTarget());
		setData(otherPi.getData());
	}

}
