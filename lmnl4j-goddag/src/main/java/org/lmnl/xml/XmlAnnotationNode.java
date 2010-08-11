package org.lmnl.xml;

import java.util.Stack;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.lmnl.AnnotationNode;
import org.lmnl.AnnotationNodeFactory;
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

	public void importFromStream(XMLStreamReader source) throws XMLStreamException {
		final XmlAnnotationNodeFactory factory = (XmlAnnotationNodeFactory) getNodeFactory();

		StringBuilder characterBuf = null;
		Stack<XmlAnnotationNode> parents = new Stack<XmlAnnotationNode>();
		parents.push(this);

		for (; source.hasNext(); source.next()) {
			switch (source.getEventType()) {
			case XMLStreamConstants.START_ELEMENT:
				if (characterBuf != null) {
					Text text = factory.createNode(Text.class, getRoot());
					text.setContent(characterBuf.toString());

					parents.peek().add(text);
					characterBuf = null;
				}
				String ns = defaultNamespace(source.getNamespaceURI(), "");
				Element element = factory.createNode(Element.class, getRoot());
				element.setNamespace(ns);
				element.setName(source.getLocalName());
				for (int ac = 0; ac < source.getAttributeCount(); ac++) {
					Attribute attr = factory.createNode(Attribute.class, getRoot());
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
					Text text = factory.createNode(Text.class, getRoot());
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
				Comment comment = factory.createNode(Comment.class, getRoot());
				comment.setComment(source.getText());

				parents.peek().add(comment);
				break;
			case XMLStreamConstants.PROCESSING_INSTRUCTION:
				ProcessingInstruction pi = factory.createNode(ProcessingInstruction.class, getRoot());
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
		for (XmlAnnotationNode node : XmlAnnotationNodeFilter.filter(sources)) {
			if (node instanceof Attribute) {
				continue;
			}
			node.exportToStream(destination);
		}
	}

	private static String defaultNamespace(String ns, String defaultNs) {
		return (ns == null ? defaultNs : ns);
	}
}
