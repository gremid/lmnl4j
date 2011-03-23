package org.lmnl.event;

import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

import org.lmnl.AnnotationFinder;
import org.lmnl.Layer;
import org.lmnl.Range;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EventGenerator {
	private Predicate<Layer> filter = Predicates.alwaysTrue();
	private AnnotationFinder annotationFinder;

	public EventGenerator() {
	}

	public void setFilter(Predicate<Layer> filter) {
		this.filter = filter;
	}

	public void setAnnotationRepository(AnnotationFinder annotationFinder) {
		this.annotationFinder = annotationFinder;
	}

	public void generate(Layer layer, EventHandler eventHandler) throws EventHandlerException {
		generate(layer, eventHandler, 0);
	}
	
	protected void generate(Layer layer, EventHandler eventHandler, int depth) throws EventHandlerException {
		final SortedMap<Integer, List<Layer>> opened = Maps.newTreeMap();
		for (Layer annotation : Iterables.filter(annotationFinder.find(layer, null, null), filter)) {
			final Range annotationRange = annotation.getRange();
			final int start = annotationRange.getStart();
			final int end = annotationRange.getEnd();

			for (Iterator<Integer> endingAtIt = opened.keySet().iterator(); endingAtIt.hasNext();) {
				final Integer endingAt = endingAtIt.next();
				if (endingAt > start) {
					break;
				}
				for (Layer ending : opened.get(endingAt)) {
					eventHandler.endAnnotation(ending, depth);
				}
				endingAtIt.remove();
			}

			eventHandler.startAnnotation(annotation, depth);

			generate(annotation, eventHandler, depth + 1);

			if (start == end) {
				eventHandler.endAnnotation(annotation, depth);
			} else {
				List<Layer> endingAnnotations = opened.get(end);
				if (endingAnnotations == null) {
					opened.put(end, endingAnnotations = Lists.newArrayList());
				}
				endingAnnotations.add(annotation);
			}
		}
		for (List<Layer> remaining : opened.values()) {
			for (Layer annotation : remaining) {
				eventHandler.endAnnotation(annotation, depth);
			}
		}
	}
}
