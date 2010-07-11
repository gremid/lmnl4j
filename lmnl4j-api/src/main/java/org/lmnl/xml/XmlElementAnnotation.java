package org.lmnl.xml;

public interface XmlElementAnnotation extends XmlAttributedAnnotation {

	/**
	 * Yields the address of the XML node, which served as the source for an
	 * annotation.
	 * 
	 * @return the XML node's address
	 */
	XPathAddress getXmlNodeAddress();
}
