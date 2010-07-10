package org.lmnl.xml;

import java.net.URI;

import org.lmnl.LmnlAnnotation;
import org.lmnl.LmnlLayer;
import org.xml.sax.Attributes;

public class DefaultXmlElementAnnotationFactory implements XmlElementAnnotationFactory {

	private final LmnlLayer destination;

	public DefaultXmlElementAnnotationFactory(LmnlLayer destination) {
		this.destination = destination;
	}

	@Override
	public LmnlAnnotation startElement(URI ns, String prefix, String localName, Attributes attr, XPathAddress xPathAddress, int startOffset) {
		return destination.add(new DefaultXmlElementAnnotation(ns, prefix, localName, attr, xPathAddress, startOffset));
	}

	@Override
	public void endElement(LmnlAnnotation element, int endOffset) {
		element.address().end = endOffset;
	}

	@Override
	public void createText(String text) {
		destination.setText(text);
	}
}
