package org.lmnl;

import java.util.Set;

public interface AnnotationRepository {

	Iterable<Annotation> find(Annotation annotation, Set<QName> names, Set<Range> ranges, boolean overlapping);
	
	Iterable<Annotation> find(Annotation annotation, Set<QName> names, Set<Range> ranges);
	
	Iterable<Annotation> find(Annotation annotation, Set<QName> names);
	
	Iterable<Annotation> find(Annotation annotation, QName name);

	Iterable<Annotation> find(Annotation annotation);

}
