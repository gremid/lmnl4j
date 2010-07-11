package org.lmnl.xml;

import java.util.Set;

import org.lmnl.LmnlAnnotation;

public interface XmlAttributedAnnotation extends LmnlAnnotation {
	Set<XmlAttribute> getAttributes();

}
