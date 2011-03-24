package org.lmnl;

import java.util.Set;

public interface AnnotationFinder {

	Iterable<Layer> find(Layer layer, Set<QName> names, Set<Range> ranges);
}
