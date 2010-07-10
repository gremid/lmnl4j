package org.lmnl.xml;

import java.net.URI;

import org.lmnl.lom.LmnlAnnotation;
import org.lmnl.lom.LmnlLayer;

public class GenericXmlBasedLmnlAnnotationFactory implements XmlBasedLmnlAnnotationFactory {

	private final LmnlLayer destination;

	public GenericXmlBasedLmnlAnnotationFactory(LmnlLayer destination) {
		this.destination = destination;
	}

	@Override
	public LmnlAnnotation createAttributeAnnotation(LmnlAnnotation elementAnnotation, URI ns, String prefix, String localName, String value) {
		return elementAnnotation.add(new XmlAttribute(ns, prefix, localName, value));
	}

	@Override
	public LmnlAnnotation createElementRange(URI ns, String prefix, String localName, int startOffset) {
		return destination.add(new XmlElement(ns, prefix, localName, startOffset));
	}

	@Override
	public void createText(String text) {
		destination.setText(text);
	}
}
