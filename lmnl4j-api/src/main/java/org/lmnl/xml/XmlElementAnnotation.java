package org.lmnl.xml;

import java.util.Set;

import org.lmnl.lom.LmnlAnnotation;

public interface XmlElementAnnotation extends LmnlAnnotation {

	/**
	 * Yields the address of the XML node, which served as the source for an
	 * annotation.
	 * 
	 * @return the XML node's address
	 */
	XPathAddress getXmlNodeAddress();

	Set<XmlAttribute> getAttributes();
}
