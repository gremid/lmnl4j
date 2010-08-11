package org.lmnl.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.XMLConstants;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.NamespaceResolver;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.lmnl.AnnotationNode;

public class XmlAnnotationNodePointer extends NodePointer {

	private static final long serialVersionUID = -5848200729651492867L;

	private XmlAnnotationNode node;
	private NamespaceResolver localNamespaceResolver;
	private String id;

	public XmlAnnotationNodePointer(XmlAnnotationNode node, Locale locale) {
		super(null, locale);
		this.node = node;
	}

	public XmlAnnotationNodePointer(XmlAnnotationNode node, Locale locale, String id) {
		super(null, locale);
		this.node = node;
		this.id = id;
	}

	public XmlAnnotationNodePointer(NodePointer parent, XmlAnnotationNode node) {
		super(parent);
		this.node = node;
	}

	public NodeIterator childIterator(NodeTest test, boolean reverse, NodePointer startWith) {
		return new XmlAnnotationNodeIterator(this, test, reverse, startWith);
	}

	public NodeIterator attributeIterator(QName name) {
		return new XmlAnnotationNodeAttributeIterator(this, name);
	}

	public QName getName() {
		String ln = null;
		String ns = null;
		if (node instanceof Element) {
			ns = XmlAnnotationNodePointer.getPrefix(node);
			ln = XmlAnnotationNodePointer.getLocalName(node);
		} else if (node instanceof ProcessingInstruction) {
			ln = ((ProcessingInstruction) node).getTarget();
		}
		return new QName(ns, ln);
	}

	public String getNamespaceURI() {
		return getNamespaceURI(node);
	}

	public synchronized NamespaceResolver getNamespaceResolver() {
		if (localNamespaceResolver == null) {
			localNamespaceResolver = new NamespaceResolver(super.getNamespaceResolver());
			localNamespaceResolver.setNamespaceContextPointer(this);
		}
		return localNamespaceResolver;
	}

	public String getNamespaceURI(String prefix) {
		return null;
	}

	public String getDefaultNamespaceURI() {
		return null;
	}

	public Object getBaseValue() {
		return node;
	}

	public Object getImmediateNode() {
		return node;
	}

	public boolean isActual() {
		return true;
	}

	public boolean isCollection() {
		return false;
	}

	public int getLength() {
		return 1;
	}

	public boolean isLeaf() {
		return !XmlAnnotationNodeFilter.filter(node).iterator().hasNext();
	}

	public boolean isLanguage(String lang) {
		String current = null;
		AnnotationNode n = node;
		while (n != null) {
			if (n instanceof Element) {
				Element e = (Element) n;
				for (Attribute attr : e.getAttributes()) {
					if (XMLConstants.XML_NS_URI.equals(attr.getNamespace()) && "lang".equals(attr.getName())) {
						current = attr.getValue();
						break;
					}
				}
			}
			if (current != null) {
				break;
			} else {
				n = n.getParentNode();
			}
		}
		return (current == null ? super.isLanguage(lang) : current.toUpperCase(Locale.ENGLISH).startsWith(
				lang.toUpperCase(Locale.ENGLISH)));
	}

	public void setValue(Object value) {
		throw new UnsupportedOperationException("Cannot modify annotation nodes");
	}

	public String asPath() {
		if (id != null) {
			return "id('" + escape(id) + "')";
		}

		StringBuffer buffer = new StringBuffer();
		if (parent != null) {
			buffer.append(parent.asPath());
		}
		if (node instanceof Element) {
			if (parent instanceof XmlAnnotationNodePointer) {
				if (buffer.length() == 0 || buffer.charAt(buffer.length() - 1) != '/') {
					buffer.append('/');
				}
				String ln = XmlAnnotationNodePointer.getLocalName(node);
				String nsURI = getNamespaceURI();
				if (nsURI == null) {
					buffer.append(ln);
					buffer.append('[');
					buffer.append(getRelativePositionByName()).append(']');
				} else {
					String prefix = getNamespaceResolver().getPrefix(nsURI);
					if (prefix != null) {
						buffer.append(prefix);
						buffer.append(':');
						buffer.append(ln);
						buffer.append('[');
						buffer.append(getRelativePositionByName());
						buffer.append(']');
					} else {
						buffer.append("node()");
						buffer.append('[');
						buffer.append(getRelativePositionOfElement());
						buffer.append(']');
					}
				}
			}
		} else if (node instanceof Text) {
			buffer.append("/text()");
			buffer.append('[');
			buffer.append(getRelativePositionOfTextNode()).append(']');
		} else if (node instanceof ProcessingInstruction) {
			buffer.append("/processing-instruction(\'");
			buffer.append(((ProcessingInstruction) node).getTarget()).append("')");
			buffer.append('[');
			buffer.append(getRelativePositionOfPI()).append(']');
		}
		return buffer.toString();
	}

	public boolean testNode(NodeTest test) {
		return testNode(node, test);
	}

	/**
	 * Test a Node.
	 * 
	 * @param node
	 *                to test
	 * @param test
	 *                to execute
	 * @return true if node passes test
	 */
	public static boolean testNode(XmlAnnotationNode node, NodeTest test) {
		if (test == null) {
			return true;
		}
		if (test instanceof NodeNameTest) {
			if (!(node instanceof XmlNamedAnnotationNode)) {
				return false;
			}

			NodeNameTest nodeNameTest = (NodeNameTest) test;
			QName testName = nodeNameTest.getNodeName();
			String namespaceURI = nodeNameTest.getNamespaceURI();
			boolean wildcard = nodeNameTest.isWildcard();
			String testPrefix = testName.getPrefix();
			if (wildcard && testPrefix == null) {
				return true;
			}
			if (wildcard || testName.getName().equals(XmlAnnotationNodePointer.getLocalName(node))) {
				String nodeNS = XmlAnnotationNodePointer.getNamespaceURI(node);
				return equalStrings(namespaceURI, nodeNS) || nodeNS == null
						&& equalStrings(testPrefix, XmlAnnotationNodePointer.getPrefix(node));
			}
			return false;
		}
		if (test instanceof NodeTypeTest) {
			switch (((NodeTypeTest) test).getNodeType()) {
			case Compiler.NODE_TYPE_NODE:
				return (node instanceof XmlAnnotationNode);
			case Compiler.NODE_TYPE_TEXT:
				return (node instanceof Text);
			case Compiler.NODE_TYPE_COMMENT:
				return (node instanceof Comment);
			case Compiler.NODE_TYPE_PI:
				return (node instanceof ProcessingInstruction);
			default:
				return false;
			}
		}
		if (test instanceof ProcessingInstructionTest && (node instanceof ProcessingInstruction)) {
			String testPI = ((ProcessingInstructionTest) test).getTarget();
			String nodePI = ((ProcessingInstruction) node).getTarget();
			return testPI.equals(nodePI);
		}
		return false;
	}

	private int getRelativePositionByName() {
		int count = 1;
		if (node instanceof XmlNamedAnnotationNode) {
			String name = ((XmlNamedAnnotationNode) node).getName();
			AnnotationNode n = node.getPreviousSibling();
			while (n != null) {
				if (n instanceof Element) {
					String nm = ((Element) n).getName();
					if (nm.equals(name)) {
						count++;
					}
				}
				n = n.getPreviousSibling();
			}
		}
		return count;
	}

	private int getRelativePositionOfElement() {
		int count = 1;
		AnnotationNode n = node.getPreviousSibling();
		while (n != null) {
			if (n instanceof Element) {
				count++;
			}
			n = n.getPreviousSibling();
		}
		return count;
	}

	private int getRelativePositionOfTextNode() {
		int count = 1;
		AnnotationNode n = node.getPreviousSibling();
		while (n != null) {
			if (n instanceof Text) {
				count++;
			}
			n = n.getPreviousSibling();
		}
		return count;
	}

	private int getRelativePositionOfPI() {
		int count = 1;
		String target = ((ProcessingInstruction) node).getTarget();
		AnnotationNode n = node.getPreviousSibling();
		while (n != null) {
			if ((n instanceof ProcessingInstruction) && ((ProcessingInstruction) n).getTarget().equals(target)) {
				count++;
			}
			n = n.getPreviousSibling();
		}
		return count;
	}

	private static boolean equalStrings(String s1, String s2) {
		if (s1 == s2) {
			return true;
		}
		s1 = s1 == null ? "" : s1.trim();
		s2 = s2 == null ? "" : s2.trim();
		return s1.equals(s2);
	}

	public boolean equals(Object object) {
		if (object != null && object instanceof XmlAnnotationNodePointer) {
			return node.equals(((XmlAnnotationNodePointer) object).node);
		}
		return super.equals(object);
	}

	public static String getPrefix(XmlAnnotationNode node) {
		return null;
	}

	public int hashCode() {
		return node.hashCode();
	}

	public static String getLocalName(XmlAnnotationNode node) {
		if (node instanceof XmlNamedAnnotationNode) {
			return ((XmlNamedAnnotationNode) node).getName();
		}
		return null;
	}

	public static String getNamespaceURI(XmlAnnotationNode node) {
		if (node instanceof XmlNamedAnnotationNode) {
			return ((XmlNamedAnnotationNode) node).getNamespace();
		}

		return null;
	}

	public Object getValue() {
		if (node instanceof Comment) {
			return ((Comment) node).getComment();
		} else if (node instanceof ProcessingInstruction) {
			String text = ((ProcessingInstruction) node).getData();
			return (text == null ? "" : text);
		}
		return node.getTextContent();
	}

	public Pointer getPointerByID(JXPathContext context, String id) {
		return new NullPointer(getLocale(), id);
	}

	public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
		XmlAnnotationNode node1 = (XmlAnnotationNode) pointer1.getBaseValue();
		XmlAnnotationNode node2 = (XmlAnnotationNode) pointer2.getBaseValue();
		if (node1.equals(node2)) {
			return 0;
		}

		if ((node1 instanceof Attribute) && !(node2 instanceof Attribute)) {
			return -1;
		}
		if (!(node1 instanceof Attribute) && (node2 instanceof Attribute)) {
			return 1;
		}
		if ((node1 instanceof Attribute) && (node2 instanceof Attribute)) {
			for (XmlAnnotationNode child : XmlAnnotationNodeFilter.filter(node)) {
				if (child.equals(node1)) {
					return -1;
				}
				if (child.equals(node2)) {
					return 1;
				}
			}
			return 0; // Should not happen
		}

		AnnotationNode current = node.getFirstChild();
		while (current != null) {
			if (current.equals(node1)) {
				return -1;
			}
			if (current.equals(node2)) {
				return 1;
			}
			current = current.getNextSibling();
		}
		return 0;
	}

	public static class XmlAnnotationNodeIterator implements NodeIterator {
		private NodePointer parent;
		private NodeTest nodeTest;
		private XmlAnnotationNode node;
		private XmlAnnotationNode child = null;
		private boolean reverse;
		private int position = 0;

		public XmlAnnotationNodeIterator(NodePointer parent, NodeTest nodeTest, boolean reverse, NodePointer startWith) {
			this.parent = parent;
			this.node = (XmlAnnotationNode) parent.getNode();
			if (startWith != null) {
				this.child = (XmlAnnotationNode) startWith.getNode();
			}
			this.nodeTest = nodeTest;
			this.reverse = reverse;
		}

		public NodePointer getNodePointer() {
			if (position == 0) {
				setPosition(1);
			}
			return child == null ? null : new XmlAnnotationNodePointer(parent, child);
		}

		public int getPosition() {
			return position;
		}

		public boolean setPosition(int position) {
			while (this.position < position) {
				if (!next()) {
					return false;
				}
			}
			while (this.position > position) {
				if (!previous()) {
					return false;
				}
			}
			return true;
		}

		private boolean previous() {
			position--;
			if (!reverse) {
				if (position == 0) {
					child = null;
				} else if (child == null) {
					child = (XmlAnnotationNode) node.getLastChild();
				} else {
					child = (XmlAnnotationNode) child.getPreviousSibling();
				}
				while (child != null && !testChild()) {
					child = (XmlAnnotationNode) child.getPreviousSibling();
				}
			} else {
				child = (XmlAnnotationNode) child.getNextSibling();
				while (child != null && !testChild()) {
					child = (XmlAnnotationNode) child.getNextSibling();
				}
			}
			return child != null;
		}

		private boolean next() {
			position++;
			if (!reverse) {
				if (position == 1) {
					if (child == null) {
						child = (XmlAnnotationNode) node.getFirstChild();
					} else {
						child = (XmlAnnotationNode) child.getNextSibling();
					}
				} else {
					child = (XmlAnnotationNode) child.getNextSibling();
				}
				while (child != null && !testChild()) {
					child = (XmlAnnotationNode) child.getNextSibling();
				}
			} else {
				if (position == 1) {
					if (child == null) {
						child = (XmlAnnotationNode) node.getLastChild();
					} else {
						child = (XmlAnnotationNode) child.getPreviousSibling();
					}
				} else {
					child = (XmlAnnotationNode) child.getPreviousSibling();
				}
				while (child != null && !testChild()) {
					child = (XmlAnnotationNode) child.getPreviousSibling();
				}
			}
			return child != null;
		}

		private boolean testChild() {
			return XmlAnnotationNodePointer.testNode(child, nodeTest);
		}
	}

	public class XmlAnnotationNodeAttributeIterator implements NodeIterator {
		private NodePointer parent;
		private List<Attribute> attributes = new ArrayList<Attribute>();
		private int position = 0;

		public XmlAnnotationNodeAttributeIterator(NodePointer parent, QName name) {
			this.parent = parent;
			XmlAnnotationNode parentNode = (XmlAnnotationNode) parent.getNode();
			if (parentNode instanceof Element) {
				Element element = (Element) parentNode;
				String prefix = name.getPrefix();
				String namespace = null;
				if (prefix != null) {
					namespace = parent.getNamespaceResolver().getNamespaceURI(prefix);
				}
				NodeNameTest nameTest = new NodeNameTest(name, namespace);
				
				for (Attribute attr : element.getAttributes()) {
					if (testNode(attr, nameTest)) {
						attributes.add(attr);
					}
				}
			}
		}

		public NodePointer getNodePointer() {
			if (position == 0) {
				if (!setPosition(1)) {
					return null;
				}
				position = 0;
			}
			int index = position - 1;
			if (index < 0) {
				index = 0;
			}
			return new XmlAnnotationNodePointer(parent, attributes.get(index));
		}

		public int getPosition() {
			return position;
		}

		public boolean setPosition(int position) {
			this.position = position;
			return position >= 1 && position <= attributes.size();
		}
	}

}
