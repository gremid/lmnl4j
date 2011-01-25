package org.lmnl;

public interface AnnotationFactory {

	<T extends Annotation> T create(Layer owner, String prefix, String localName, String text, Range address,
			Class<T> type);
	
	void destroy(Annotation annotation);
}
