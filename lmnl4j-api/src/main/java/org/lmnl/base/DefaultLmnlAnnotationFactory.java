package org.lmnl.base;

import org.lmnl.LmnlAnnotation;
import org.lmnl.LmnlAnnotationFactory;
import org.lmnl.LmnlLayer;
import org.lmnl.LmnlRange;

import com.google.common.base.Preconditions;

public class DefaultLmnlAnnotationFactory implements LmnlAnnotationFactory {

	@SuppressWarnings("unchecked")
	public <T extends LmnlAnnotation> T create(LmnlLayer owner, String prefix, String localName, String text,
			LmnlRange address, Class<T> type) {
		Preconditions.checkArgument(type.isAssignableFrom(DefaultLmnlAnnotation.class));
		return (T) new DefaultLmnlAnnotation(owner, prefix, localName, text, address);
	}

	public void destroy(LmnlAnnotation annotation) {
		Preconditions.checkArgument(annotation instanceof DefaultLmnlAnnotation);
		((DefaultLmnlAnnotation) annotation).destroy();
	}
}
