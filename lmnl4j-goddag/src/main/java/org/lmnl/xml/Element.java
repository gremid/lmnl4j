package org.lmnl.xml;

import java.util.LinkedList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.lmnl.AnnotationNode;
import org.lmnl.AnnotationNodeFactory;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.Predicate;
import org.neo4j.helpers.collection.FilteringIterable;
import org.neo4j.helpers.collection.IterableWrapper;

public class Element extends XmlNamedAnnotationNode {
	public static final String NODE_TYPE = "xml:element";

	public Element(AnnotationNodeFactory nodeFactory, Node node, AnnotationNode root) {
		super(nodeFactory, node, root);
	}

	public Iterable<Attribute> getAttributes() {
		return new IterableWrapper<Attribute, AnnotationNode>(new FilteringIterable<AnnotationNode>(
				(Iterable<AnnotationNode>) this, new Predicate<AnnotationNode>() {

					@Override
					public boolean accept(AnnotationNode item) {
						return (item instanceof Attribute);
					}
				})) {

			@Override
			protected Attribute underlyingObjectToObject(AnnotationNode object) {
				return (Attribute) object;
			}
		};
	}

	@Override
	public String getTextContent() {
		StringBuilder str = new StringBuilder();
		for (AnnotationNode child : this) {
			if ((child instanceof XmlAnnotationNode) && !(child instanceof Attribute)) {
				str.append(((XmlAnnotationNode) child).getTextContent());
			}
		}
		return str.toString();
	}

	@Override
	public String toString() {
		return "<" + getName() + "/> [" + getUnderlyingNode() + "]";
	}

	@Override
	public void exportToStream(XMLStreamWriter destination) throws XMLStreamException {
		boolean hasChildren = false;
		List<Attribute> attributes = new LinkedList<Attribute>();
		for (XmlAnnotationNode child : XmlAnnotationNodeFilter.filter(getChildNodes())) {
			if (child instanceof Attribute) {
				attributes.add((Attribute) child);
			} else {
				hasChildren = true;
			}
		}

		String elementNs = getNamespace();
		if ("".equals(elementNs)) {
			if (hasChildren) {
				destination.writeStartElement(getName());
			} else {
				destination.writeEmptyElement(getName());
			}
		} else if (XMLConstants.XML_NS_URI.equals(elementNs)) {
			if (hasChildren) {
				destination.writeStartElement(XMLConstants.XML_NS_PREFIX, getName(), elementNs);
			} else {
				destination.writeEmptyElement(XMLConstants.XML_NS_PREFIX, getName(), elementNs);
			}
		} else {
			if (hasChildren) {
				destination.writeStartElement(elementNs, getName());
			} else {
				destination.writeEmptyElement(elementNs, getName());
			}
		}
		for (Attribute attribute : attributes) {
			String attributeNs = attribute.getNamespace();
			if ("".equals(attributeNs)) {
				destination.writeAttribute(attribute.getName(), attribute.getValue());
			} else if (XMLConstants.XML_NS_URI.equals(attributeNs)) {
				destination.writeAttribute(XMLConstants.XML_NS_PREFIX, attributeNs, attribute.getName(),
						attribute.getValue());
			} else {
				destination.writeAttribute(attributeNs, attribute.getName(), attribute.getValue());
			}

		}
		if (hasChildren) {
			exportToStream(getChildNodes(), destination);
			destination.writeEndElement();
		}
	}
}
