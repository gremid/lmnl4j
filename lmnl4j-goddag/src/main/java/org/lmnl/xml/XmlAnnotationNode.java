package org.lmnl.xml;

import java.util.List;
import java.util.Stack;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.jaxen.JaxenException;
import org.lmnl.AnnotationNode;
import org.lmnl.AnnotationNodeFactory;
import org.neo4j.graphdb.Node;

public abstract class XmlAnnotationNode extends AnnotationNode {
	public XmlAnnotationNode(AnnotationNodeFactory nodeFactory, Node node, long owner) {
		super(nodeFactory, node, owner);
	}

	public String getTextContent() {
		StringBuilder text = new StringBuilder();
		for (AnnotationNode child : this) {
			if (child instanceof XmlAnnotationNode) {
				text.append(((XmlAnnotationNode) child).getTextContent());
			}
		}
		return text.toString();
	}

	public XmlAnnotationNodeXPath xpath(String xpath) throws JaxenException {
		// FIXME: Jaxen does not obey document order in returning result node sets!
		return new XmlAnnotationNodeXPath(xpath, getOwner());
	}
	@SuppressWarnings("unchecked")
	public List<XmlAnnotationNode> selectAllNodes(XmlAnnotationNodeXPath xpath) throws JaxenException {
		return xpath.selectNodes(this);
	}

	public List<XmlAnnotationNode> selectAllNodes(String xpath) throws JaxenException {
		return selectAllNodes(xpath(xpath));
	}

	public XmlAnnotationNode selectSingleNode(XmlAnnotationNodeXPath xpath) throws JaxenException {
		return (XmlAnnotationNode) xpath.selectSingleNode(this);
	}

	public XmlAnnotationNode selectSingleNode(String xpath) throws JaxenException {
		return selectSingleNode(xpath(xpath));
	}

	public void importFromStream(XMLStreamReader source) throws XMLStreamException {
		final XmlAnnotationNodeFactory factory = (XmlAnnotationNodeFactory) getNodeFactory();
		final long owner = getOwner();

		StringBuilder characterBuf = null;
		Stack<XmlAnnotationNode> parents = new Stack<XmlAnnotationNode>();
		parents.push(this);

		for (; source.hasNext(); source.next()) {
			switch (source.getEventType()) {
			case XMLStreamConstants.START_ELEMENT:
				if (characterBuf != null) {
					Text text = factory.createNode(Text.class, owner);
					text.setContent(characterBuf.toString());

					parents.peek().add(text);
					characterBuf = null;
				}
				String ns = defaultNamespace(source.getNamespaceURI(), "");
				Element element = factory.createNode(Element.class, owner);
				element.setNamespace(ns);
				element.setName(source.getLocalName());
				for (int ac = 0; ac < source.getAttributeCount(); ac++) {
					Attribute attr = factory.createNode(Attribute.class, owner);
					attr.setNamespace(defaultNamespace(source.getAttributeNamespace(ac), ns));
					attr.setName(source.getAttributeLocalName(ac));
					attr.setValue(source.getAttributeValue(ac));
					element.add(attr);
				}
				parents.peek().add(element);
				parents.push(element);
				break;
			case XMLStreamConstants.END_ELEMENT:
				if (characterBuf != null) {
					Text text = factory.createNode(Text.class, owner);
					text.setContent(characterBuf.toString());

					parents.peek().add(text);
					characterBuf = null;
				}
				parents.pop();
				break;
			case XMLStreamConstants.CDATA:
			case XMLStreamConstants.ENTITY_REFERENCE:
			case XMLStreamConstants.CHARACTERS:
				if (characterBuf == null) {
					characterBuf = new StringBuilder();
				}
				characterBuf.append(source.getText());
				break;
			case XMLStreamConstants.COMMENT:
				Comment comment = factory.createNode(Comment.class, owner);
				comment.setComment(source.getText());

				parents.peek().add(comment);
				break;
			case XMLStreamConstants.PROCESSING_INSTRUCTION:
				ProcessingInstruction pi = factory.createNode(ProcessingInstruction.class, owner);
				pi.setTarget(source.getPITarget());
				pi.setData(source.getPIData());

				parents.peek().add(pi);
				break;
			}
		}
	}

	public void exportToStream(XMLStreamWriter destination) throws XMLStreamException {
		exportToStream(getChildNodes(), destination);
	}

	public static void exportToStream(Iterable<AnnotationNode> sources, XMLStreamWriter destination) throws XMLStreamException {
		for (AnnotationNode node : sources) {
			if (node instanceof Attribute) {
				continue;
			} else if (node instanceof XmlAnnotationNode) {
				((XmlAnnotationNode) node).exportToStream(destination);
			}

		}
	}

	private static String defaultNamespace(String ns, String defaultNs) {
		return (ns == null ? defaultNs : ns);
	}
}
