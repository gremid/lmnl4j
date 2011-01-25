package org.lmnl.event;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.lmnl.LmnlAnnotation;
import org.lmnl.LmnlDocument;
import org.lmnl.LmnlLayer;
import org.lmnl.LmnlRange;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class LmnlEventGenerator {
	private Predicate<LmnlLayer> filter = Predicates.alwaysTrue();

	public LmnlEventGenerator() {
	}

	public LmnlEventGenerator(Predicate<LmnlLayer> filter) {
		this.filter = filter;
	}

	public void generate(LmnlLayer layer, LmnlEventHandler eventHandler) throws LmnlEventHandlerException {
		generate(layer, eventHandler, new Ordering<LmnlAnnotation>() {

			public int compare(LmnlAnnotation o1, LmnlAnnotation o2) {
				return o1.address().compareTo(o2.address());
			}
		});
	}

	public void generate(LmnlLayer layer, LmnlEventHandler eventHandler, Ordering<LmnlAnnotation> rangeOrdering) throws LmnlEventHandlerException {
		if (!filter.apply(layer)) {
			return;
		}

		if (layer instanceof LmnlDocument) {
			eventHandler.startDocument((LmnlDocument) layer);
		}
		if (layer instanceof LmnlAnnotation) {
			eventHandler.startAnnotation((LmnlAnnotation) layer);
		}

		List<LmnlAnnotation> children = Lists.newArrayList(layer);

		SortedMap<Integer, List<LmnlAnnotation>> offsetIndex = new TreeMap<Integer, List<LmnlAnnotation>>();
		for (LmnlAnnotation r : children) {
			LmnlRange addr = r.address();
			if (offsetIndex.containsKey(addr.start)) {
				offsetIndex.get(addr.start).add(r);
			} else {
				List<LmnlAnnotation> rangeList = new ArrayList<LmnlAnnotation>();
				rangeList.add(r);
				offsetIndex.put(addr.start, rangeList);
			}
			if (addr.end != addr.start) {
				if (offsetIndex.containsKey(addr.end)) {
					offsetIndex.get(addr.end).add(r);
				} else {
					List<LmnlAnnotation> rangeList = new ArrayList<LmnlAnnotation>();
					rangeList.add(r);
					offsetIndex.put(addr.end, rangeList);
				}
			}
		}

		for (int offset : offsetIndex.keySet()) {
			List<LmnlAnnotation> ranges = offsetIndex.get(offset);
			// ending ranges
			for (LmnlAnnotation annotation : rangeOrdering.reverse().sortedCopy(ranges)) {
				LmnlRange address = annotation.address();
				if (address.end == offset && address.start != offset) {
					eventHandler.endAnnotation(annotation);
				}
			}
			// atoms ranges
			for (LmnlAnnotation annotation : rangeOrdering.sortedCopy(ranges)) {
				LmnlRange address = annotation.address();
				if (address.start == offset && address.end == offset) {
					eventHandler.startAnnotation(annotation);
					for (LmnlAnnotation a : annotation) {
						generate(a, eventHandler, rangeOrdering);
					}
					eventHandler.endAnnotation(annotation);
				}
			}
			// starting ranges
			for (LmnlAnnotation annotation : rangeOrdering.sortedCopy(ranges)) {
				LmnlRange address = annotation.address();
				if (address.start == offset && address.end != offset) {
					eventHandler.startAnnotation(annotation);
					for (LmnlAnnotation a : annotation) {
						generate(a, eventHandler, rangeOrdering);
					}
				}
			}
		}

		if (layer instanceof LmnlDocument) {
			eventHandler.endDocument((LmnlDocument) layer);
		}
		if (layer instanceof LmnlAnnotation) {
			eventHandler.endAnnotation((LmnlAnnotation) layer);
		}
	}
}
