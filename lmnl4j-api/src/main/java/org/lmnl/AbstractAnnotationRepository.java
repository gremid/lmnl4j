package org.lmnl;

import java.util.Collections;
import java.util.Set;

public abstract class AbstractAnnotationRepository implements AnnotationRepository {


	public Iterable<Annotation> find(Annotation annotation, Set<QName> names, Set<Range> ranges) {
		return find(annotation, names, ranges, true);
	}
	
	public Iterable<Annotation> find(Annotation annotation, Set<QName> names) {
		return find(annotation, names, null, true);
	}
	
	public Iterable<Annotation> find(Annotation annotation, QName name) {
		return find(annotation, Collections.singleton(name), null, true);
	}

	public Iterable<Annotation> find(Annotation annotation) {
		return find(annotation, null, null, true);
	}

}
