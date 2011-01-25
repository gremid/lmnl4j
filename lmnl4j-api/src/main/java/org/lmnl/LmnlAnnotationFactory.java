package org.lmnl;

public interface LmnlAnnotationFactory {

	<T extends LmnlAnnotation> T create(LmnlLayer owner, String prefix, String localName, String text, LmnlRange address,
			Class<T> type);
	
	void destroy(LmnlAnnotation annotation);
}
