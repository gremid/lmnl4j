package org.lmnl.xml;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;

public class XmlAnnotationNodeXPath extends BaseXPath {

	private static final long serialVersionUID = 4349240157748544274L;

	protected XmlAnnotationNodeXPath(String xpath, long owner) throws JaxenException {
		super(xpath, new XmlAnnotationNodeNavigator(owner));
	}

}
