package org.lmnl.xml;

import java.util.Locale;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;
import org.apache.commons.jxpath.ri.model.dom.DOMPointerFactory;
import org.lmnl.AnnotationNode;

public class XmlAnnotationNodePointerFactory implements NodePointerFactory {
	public static final int ORDER = (DOMPointerFactory.DOM_POINTER_FACTORY_ORDER - 1);

	@Override
	public int getOrder() {
		return ORDER;
	}

	@Override
	public NodePointer createNodePointer(QName name, Object object, Locale locale) {
		if (object instanceof XmlAnnotationNode) {
			XmlAnnotationNode node = (XmlAnnotationNode) object;
			XmlAnnotationNode parentNode = null;
			for (AnnotationNode ancestor : node.getAncestorNodes()) {
				if (ancestor instanceof XmlAnnotationNode) {
					parentNode = (XmlAnnotationNode) ancestor;
				}
			}
			if (parentNode == null) {
				return new XmlAnnotationNodePointer(node, locale);
			} else {
				QName parentName = null;
				if (parentNode instanceof XmlNamedAnnotationNode) {
					parentName = new QName(((XmlNamedAnnotationNode) parentNode).getName());
				}
				return createNodePointer(createNodePointer(parentName, parentNode, locale), name, object);
			}

		}
		return null;
	}

	@Override
	public NodePointer createNodePointer(NodePointer parent, QName name, Object object) {
		if (object instanceof XmlAnnotationNodePointer) {
			return new XmlAnnotationNodePointer(parent, (XmlAnnotationNode) object);
		}
		return null;
	}

}
