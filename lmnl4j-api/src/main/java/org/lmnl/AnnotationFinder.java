package org.lmnl;

public interface AnnotationFinder {

	Iterable<Layer> find(Layer layer, QName annotationName, Range range);
}
