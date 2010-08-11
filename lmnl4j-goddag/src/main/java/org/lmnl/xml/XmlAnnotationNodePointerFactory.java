package org.lmnl.xml;

import java.util.Locale;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;
import org.apache.commons.jxpath.ri.model.dom.DOMPointerFactory;

public class XmlAnnotationNodePointerFactory implements NodePointerFactory {
	public static final int ORDER = (DOMPointerFactory.DOM_POINTER_FACTORY_ORDER - 1);

	@Override
	public int getOrder() {
		return ORDER;
	}

	@Override
	public NodePointer createNodePointer(QName name, Object object, Locale locale) {
		return (object instanceof XmlAnnotationNode ? new XmlAnnotationNodePointer((XmlAnnotationNode) object, locale)
				: null);
	}

	@Override
	public NodePointer createNodePointer(NodePointer parent, QName name, Object object) {
		return (object instanceof XmlAnnotationNode ? new XmlAnnotationNodePointer(parent, (XmlAnnotationNode) object)
				: null);
	}

}
