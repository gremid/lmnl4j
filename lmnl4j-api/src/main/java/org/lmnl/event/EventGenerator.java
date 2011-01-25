package org.lmnl.event;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.lmnl.Annotation;
import org.lmnl.Document;
import org.lmnl.Layer;
import org.lmnl.Range;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class EventGenerator {
	private Predicate<Layer> filter = Predicates.alwaysTrue();

	public EventGenerator() {
	}

	public EventGenerator(Predicate<Layer> filter) {
		this.filter = filter;
	}

	public void generate(Layer layer, EventHandler eventHandler) throws EventHandlerException {
		generate(layer, eventHandler, new Ordering<Annotation>() {

			public int compare(Annotation o1, Annotation o2) {
				return o1.address().compareTo(o2.address());
			}
		});
	}

	public void generate(Layer layer, EventHandler eventHandler, Ordering<Annotation> rangeOrdering) throws EventHandlerException {
		if (!filter.apply(layer)) {
			return;
		}

		if (layer instanceof Document) {
			eventHandler.startDocument((Document) layer);
		}
		if (layer instanceof Annotation) {
			eventHandler.startAnnotation((Annotation) layer);
		}

		List<Annotation> children = Lists.newArrayList(layer);

		SortedMap<Integer, List<Annotation>> offsetIndex = new TreeMap<Integer, List<Annotation>>();
		for (Annotation r : children) {
			Range addr = r.address();
			if (offsetIndex.containsKey(addr.start)) {
				offsetIndex.get(addr.start).add(r);
			} else {
				List<Annotation> rangeList = new ArrayList<Annotation>();
				rangeList.add(r);
				offsetIndex.put(addr.start, rangeList);
			}
			if (addr.end != addr.start) {
				if (offsetIndex.containsKey(addr.end)) {
					offsetIndex.get(addr.end).add(r);
				} else {
					List<Annotation> rangeList = new ArrayList<Annotation>();
					rangeList.add(r);
					offsetIndex.put(addr.end, rangeList);
				}
			}
		}

		for (int offset : offsetIndex.keySet()) {
			List<Annotation> ranges = offsetIndex.get(offset);
			// ending ranges
			for (Annotation annotation : rangeOrdering.reverse().sortedCopy(ranges)) {
				Range address = annotation.address();
				if (address.end == offset && address.start != offset) {
					eventHandler.endAnnotation(annotation);
				}
			}
			// atoms ranges
			for (Annotation annotation : rangeOrdering.sortedCopy(ranges)) {
				Range address = annotation.address();
				if (address.start == offset && address.end == offset) {
					eventHandler.startAnnotation(annotation);
					for (Annotation a : annotation) {
						generate(a, eventHandler, rangeOrdering);
					}
					eventHandler.endAnnotation(annotation);
				}
			}
			// starting ranges
			for (Annotation annotation : rangeOrdering.sortedCopy(ranges)) {
				Range address = annotation.address();
				if (address.start == offset && address.end != offset) {
					eventHandler.startAnnotation(annotation);
					for (Annotation a : annotation) {
						generate(a, eventHandler, rangeOrdering);
					}
				}
			}
		}

		if (layer instanceof Document) {
			eventHandler.endDocument((Document) layer);
		}
		if (layer instanceof Annotation) {
			eventHandler.endAnnotation((Annotation) layer);
		}
	}
}
