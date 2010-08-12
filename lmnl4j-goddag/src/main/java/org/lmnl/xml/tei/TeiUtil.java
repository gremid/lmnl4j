package org.lmnl.xml.tei;

import org.apache.commons.jxpath.JXPathContext;
import org.lmnl.xml.XmlAnnotationNode;

public class TeiUtil {

	public static JXPathContext newXPathContext(XmlAnnotationNode node) {
		JXPathContext xpath = node.newXPathContext();
		xpath.registerNamespace("tei", "http://www.tei-c.org/ns/1.0");
		return xpath;
	}
}
