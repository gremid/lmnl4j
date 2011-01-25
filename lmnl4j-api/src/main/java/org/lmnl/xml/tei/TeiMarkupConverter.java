/**
 * Layered Markup and Annotation Language for Java (lmnl4j):
 * implementation of LMNL, a markup language supporting layered and/or
 * overlapping annotations.
 *
 * Copyright (C) 2010 the respective authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lmnl.xml.tei;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;

import org.lmnl.Annotation;
import org.lmnl.Document;
import org.lmnl.Layer;
import org.lmnl.Range;
import org.lmnl.base.DefaultAnnotation;
import org.lmnl.util.OverlapIndexer;
import org.lmnl.xml.XMLUtils;
import org.lmnl.xml.XMLAttribute;
import org.lmnl.xml.XMLElement;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Converts TEI-P5-specific markup elements to LMNL-native annotations.
 * 
 * <p/>
 * 
 * This converter aims to convert any TEI-P5 markup, that works around XML-related constraints (like having a single,
 * non-overlapping hierarchy) and that can be expressed more easily in the LMNL realm.
 * 
 * @author <a href="http://gregor.middell.net/" title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class TEIMarkupConverter {
	private static final URI TEI_NS = URI.create("http://www.tei-c.org/ns/1.0");
	private boolean convertLineBreaks = true;
	private boolean convertPageBreaks = true;
	private boolean convertColumnBreaks = true;
	private boolean convertSpanningElements = true;

	/**
	 * Shall linebreak elements (<code>&lt;lb/></code>) be converted to <code>lmnl:line</code> ranges.
	 * 
	 * @param convertLinebreaks
	 *                <code>true</code> (yes, default) or <code>false</code> (no)
	 */
	public void setConvertLineBreaks(boolean convertLinebreaks) {
		this.convertLineBreaks = convertLinebreaks;
	}

	/**
	 * Shall pagebreak elements (<code>&lt;pb/></code>) be converted to <code>lmnl:page</code> ranges.
	 * 
	 * @param convertPageBreaks
	 *                <code>true</code> (yes, default) or <code>false</code> (no)
	 */
	public void setConvertPageBreaks(boolean convertPageBreaks) {
		this.convertPageBreaks = convertPageBreaks;
	}

	/**
	 * Shall columnbreak elements (<code>&lt;cb/></code>) be converted to <code>lmnl:column</code> ranges.
	 * 
	 * @param convertColumnBreaks
	 *                <code>true</code> (yes, default) or <code>false</code> (no)
	 */
	public void setConvertColumnBreaks(boolean convertColumnBreaks) {
		this.convertColumnBreaks = convertColumnBreaks;
	}

	/**
	 * Shall spanning elements (those with a model of <code>att.spanning</code>) be converted to their native counterpart (for
	 * instance <code>&lt;addSpan/></code> to <code>&lt;add/></code>)?
	 * 
	 * @param convertSpanningElements
	 *                <code>true</code> (yes, default) or <code>false</code> (no)
	 */
	public void setConvertSpanningElements(boolean convertSpanningElements) {
		this.convertSpanningElements = convertSpanningElements;
	}

	/**
	 * Convert TEI-P5 specific markup in the given layer.
	 * 
	 * @param layer
	 *                the layer whose contents shall be converted
	 */
	public void convert(Layer layer) {
		if (convertLineBreaks) {
			convertSimpleMilestones(layer, "lb", "line");
		}
		if (convertPageBreaks) {
			convertSimpleMilestones(layer, "pb", "page");
		}
		if (convertColumnBreaks) {
			convertSimpleMilestones(layer, "cb", "column");
		}
		if (convertSpanningElements) {
			convertSpanningElements(layer);
		}
	}

	private void convertSimpleMilestones(Layer layer, String msElementName, String elementName) {
		final DefaultAnnotation covering = layer.add(Document.LMNL_PREFIX, "all", null, new Range(0, layer
				.getText().length()), DefaultAnnotation.class);
		final TeiElementPredicate msPredicate = new TeiElementPredicate(msElementName);

		Iterable<Annotation> ranges = Iterables.concat(Iterables.filter(layer, msPredicate),
				Collections.singletonList(covering));

		SortedMap<Range, List<Annotation>> msIndex = new OverlapIndexer(msPredicate).apply(ranges);
		for (Range segment : msIndex.keySet()) {
			for (Annotation milestone : msIndex.get(segment)) {
				if (milestone.equals(covering)) {
					continue;
				}
				if (milestone.address().start == segment.start) {
					final Annotation element = layer.add(Document.LMNL_PREFIX, elementName, null,
							new Range(segment), XMLElement.class);
					for (XMLElement annotation : Iterables.filter(milestone,
							XMLElement.class)) {
						element.add(annotation, XMLElement.class);
					}
					layer.remove(milestone);
				}
			}
		}

		layer.remove(covering);
	}

	private void convertSpanningElements(Layer layer) {
		// indexing of identifyable ranges
		final Map<URI, Annotation> ids = new HashMap<URI, Annotation>();
		for (Annotation annotation : layer) {
			if (annotation.getId() != null) {
				ids.put(annotation.getId(), annotation);
			}
		}

		// collecting spanning elements and their new ranges
		final Map<XMLElement, Range> spans = new HashMap<XMLElement, Range>();
		for (XMLElement range : layer.select(XMLElement.class)) {
			String localName = range.getLocalName();
			if (!localName.endsWith("Span") && !localName.equals("index")) {
				continue;
			}
			try {
				final XMLAttribute to = Iterables.find(range.getAttributes(), new Predicate<XMLAttribute>() {
					public boolean apply(XMLAttribute input) {
						return "spanTo".equals(input.localName) && (input.value != null);
					}
				});
				if (to == null) {
					continue;
				}
				
				final Annotation end = ids.get(new URI(to.value));
				if (end == null) {
					continue;
				}

				spans.put(range, new Range(range.address().start, end.address().start));
			} catch (NoSuchElementException e) {
			} catch (URISyntaxException e) {
			}
		}
		// transformation of spanning elements
		for (Map.Entry<XMLElement, Range> span : spans.entrySet()) {
			final XMLElement sa = span.getKey();
			String localName = sa.getLocalName();
			if (localName.endsWith("Span")) {
				localName = localName.substring(0, localName.length() - 4);
			}
			
			final XMLElement na = layer.add(sa.getPrefix(), localName, sa.getText(), span.getValue(), XMLElement.class);
			XMLUtils.copyProperties(sa, na);
			layer.remove(sa);
		}
	}

	private static class TeiElementPredicate implements Predicate<Annotation> {

		private String localName;

		private TeiElementPredicate(String localName) {
			this.localName = localName;
		}

		public boolean apply(Annotation input) {
			if (!TEI_NS.equals(input.getNamespace())) {
				return false;
			}

			return localName.equals(input.getLocalName());
		}

	}
}
