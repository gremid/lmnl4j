package org.lmnl.base;

import org.lmnl.Annotation;
import org.lmnl.AnnotationFactory;
import org.lmnl.Layer;
import org.lmnl.Range;

import com.google.common.base.Preconditions;

public class DefaultAnnotationFactory implements AnnotationFactory {

	@SuppressWarnings("unchecked")
	public <T extends Annotation> T create(Layer owner, String prefix, String localName, String text,
			Range address, Class<T> type) {
		Preconditions.checkArgument(type.isAssignableFrom(DefaultAnnotation.class));
		return (T) new DefaultAnnotation(owner, prefix, localName, text, address);
	}

	public void destroy(Annotation annotation) {
		Preconditions.checkArgument(annotation instanceof DefaultAnnotation);
		((DefaultAnnotation) annotation).destroy();
	}
}
