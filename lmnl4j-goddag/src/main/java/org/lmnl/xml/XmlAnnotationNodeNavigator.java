package org.lmnl.xml;

import java.util.Collections;
import java.util.Iterator;

import org.jaxen.DefaultNavigator;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.XPath;
import org.jaxen.saxpath.SAXPathException;
import org.lmnl.AnnotationNode;
import org.neo4j.helpers.Predicate;
import org.neo4j.helpers.collection.FilteringIterable;

public class XmlAnnotationNodeNavigator extends DefaultNavigator {

	private static final long serialVersionUID = 1259169684834172028L;
	private final long owner;

	public XmlAnnotationNodeNavigator(long owner) {
		super();
		this.owner = owner;
	}

	@Override
	public Iterator<?> getChildAxisIterator(Object contextNode) throws UnsupportedAxisException {
		if (contextNode instanceof XmlAnnotationNode) {
			Iterable<AnnotationNode> children = new XmlAnnotationNodeFilter((XmlAnnotationNode) contextNode);
			if (contextNode instanceof Element) {
				children = new FilteringIterable<AnnotationNode>(children, new Predicate<AnnotationNode>() {

					@Override
					public boolean accept(AnnotationNode item) {
						return !(item instanceof Attribute);
					}
				});
			}
			return children.iterator();
		}

		return super.getChildAxisIterator(contextNode);
	}

	@Override
	public Iterator<?> getParentAxisIterator(Object contextNode) throws UnsupportedAxisException {
		if (contextNode instanceof XmlAnnotationNode) {
			return Collections.singleton(((XmlAnnotationNode) contextNode).getParentNode()).iterator();
		}
		return super.getParentAxisIterator(contextNode);
	}

	@Override
	public Iterator<?> getAncestorAxisIterator(Object contextNode) throws UnsupportedAxisException {
		if (contextNode instanceof XmlAnnotationNode) {
			return new XmlAnnotationNodeFilter(((XmlAnnotationNode) contextNode).getAncestorNodes()).iterator();
		}
		return super.getAncestorAxisIterator(contextNode);
	}

	@Override
	public Iterator<?> getAttributeAxisIterator(Object contextNode) throws UnsupportedAxisException {
		if (contextNode instanceof Element) {
			return ((Element) contextNode).getAttributes().iterator();
		}
		return super.getAttributeAxisIterator(contextNode);
	}

	@Override
	public Object getDocumentNode(Object contextNode) {
		if (contextNode instanceof XmlAnnotationNode) {
			AnnotationNode node = (AnnotationNode) contextNode;
			while (node != null && !(node instanceof Document)) {
				node = node.getParentNode();
			}
			return node;

		}
		return super.getDocumentNode(contextNode);
	}

	@Override
	public String getProcessingInstructionData(Object obj) {
		if (obj instanceof ProcessingInstruction) {
			return ((ProcessingInstruction) obj).getData();
		}
		return super.getProcessingInstructionData(obj);
	}

	@Override
	public String getProcessingInstructionTarget(Object obj) {
		if (obj instanceof ProcessingInstruction) {
			return ((ProcessingInstruction) obj).getTarget();
		}
		return super.getProcessingInstructionTarget(obj);
	}

	@Override
	public String getAttributeName(Object node) {
		if (node instanceof Attribute) {
			return ((Attribute) node).getName();
		}
		return null;
	}

	@Override
	public String getAttributeNamespaceUri(Object node) {
		if (node instanceof Attribute) {
			return ((Attribute) node).getNamespace();
		}
		return null;
	}

	@Override
	public String getAttributeQName(Object node) {
		return getAttributeName(node);
	}

	@Override
	public String getAttributeStringValue(Object node) {
		if (node instanceof Attribute) {
			return ((Attribute) node).getValue();
		}
		return null;
	}

	@Override
	public String getCommentStringValue(Object node) {
		if (node instanceof Comment) {
			return ((Comment) node).getComment();
		}
		return null;
	}

	@Override
	public String getElementName(Object node) {
		if (node instanceof Element) {
			return ((Element) node).getName();
		}
		return null;
	}

	@Override
	public String getElementNamespaceUri(Object node) {
		if (node instanceof Element) {
			return ((Element) node).getNamespace();
		}
		return null;
	}

	@Override
	public String getElementQName(Object node) {
		return getElementName(node);
	}

	@Override
	public String getElementStringValue(Object node) {
		if (node instanceof Element) {
			return ((Element) node).getTextContent();
		}
		return null;
	}

	@Override
	public String getNamespacePrefix(Object node) {
		return null;
	}

	@Override
	public String getNamespaceStringValue(Object node) {
		return null;
	}

	@Override
	public String getTextStringValue(Object node) {
		if (node instanceof Text) {
			return ((Text) node).getContent();
		}
		return null;
	}

	@Override
	public boolean isAttribute(Object node) {
		return (node instanceof Attribute);
	}

	@Override
	public boolean isComment(Object node) {
		return (node instanceof Comment);
	}

	@Override
	public boolean isDocument(Object node) {
		return (node instanceof Document);
	}

	@Override
	public boolean isElement(Object node) {
		return (node instanceof Element);
	}

	@Override
	public boolean isNamespace(Object node) {
		return false;
	}

	@Override
	public boolean isProcessingInstruction(Object node) {
		return (node instanceof ProcessingInstruction);
	}

	@Override
	public boolean isText(Object node) {
		return (node instanceof Text);
	}

	@Override
	public XPath parseXPath(String xpath) throws SAXPathException {
		return new XmlAnnotationNodeXPath(xpath, owner);
	}

	private static class XmlAnnotationNodeFilter extends FilteringIterable<AnnotationNode> {

		public XmlAnnotationNodeFilter(Iterable<AnnotationNode> source) {
			super(source, new Predicate<AnnotationNode>() {
				@Override
				public boolean accept(AnnotationNode item) {
					return (item instanceof XmlAnnotationNode);
				}
			});
		}
	}

}
