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

import org.lmnl.LmnlAnnotation;
import org.lmnl.LmnlDocument;
import org.lmnl.LmnlLayer;
import org.lmnl.LmnlRange;
import org.lmnl.base.AbstractLmnlLayer;
import org.lmnl.base.DefaultLmnlAnnotation;
import org.lmnl.util.OverlapIndexer;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Converts TEI-P5-specific markup elements to LMNL-native annotations.
 * 
 * <p/>
 * 
 * This converter aims to convert any TEI-P5 markup, that works around
 * XML-related constraints (like having a single, non-overlapping hierarchy) and
 * that can be expressed more easily in the LMNL realm.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class TeiMarkupConverter {
	private static final URI TEI_NS = URI.create("http://www.tei-c.org/ns/1.0");
	private boolean convertLineBreaks = true;
	private boolean convertPageBreaks = true;
	private boolean convertColumnBreaks = true;
	private boolean convertSpanningElements = true;

	/**
	 * Shall linebreak elements (<code>&lt;lb/></code>) be converted to
	 * <code>lmnl:line</code> ranges.
	 * 
	 * @param convertLinebreaks
	 *                <code>true</code> (yes, default) or <code>false</code>
	 *                (no)
	 */
	public void setConvertLineBreaks(boolean convertLinebreaks) {
		this.convertLineBreaks = convertLinebreaks;
	}

	/**
	 * Shall pagebreak elements (<code>&lt;pb/></code>) be converted to
	 * <code>lmnl:page</code> ranges.
	 * 
	 * @param connvertPageBreaks
	 *                <code>true</code> (yes, default) or <code>false</code>
	 *                (no)
	 */
	public void setConvertPageBreaks(boolean connvertPageBreaks) {
		this.convertPageBreaks = connvertPageBreaks;
	}

	/**
	 * Shall columnbreak elements (<code>&lt;cb/></code>) be converted to
	 * <code>lmnl:column</code> ranges.
	 * 
	 * @param convertColumnBreaks
	 *                <code>true</code> (yes, default) or <code>false</code>
	 *                (no)
	 */
	public void setConvertColumnBreaks(boolean convertColumnBreaks) {
		this.convertColumnBreaks = convertColumnBreaks;
	}

	/**
	 * Shall spanning elements (those with a model of
	 * <code>att.spanning</code>) be converted to their native counterpart
	 * (for instance <code>&lt;addSpan/></code> to <code>&lt;add/></code>)?
	 * 
	 * @param convertSpanningElements
	 *                <code>true</code> (yes, default) or <code>false</code>
	 *                (no)
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
	public void convert(LmnlLayer layer) {
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


	private void convertSimpleMilestones(LmnlLayer layer, String msElementName, String elementName) {
		TeiElementPredicate msPredicate = new TeiElementPredicate(msElementName);

		Iterable<LmnlAnnotation> ranges = Iterables.filter(layer, msPredicate);
		ranges = Iterables.concat(ranges, Collections.singletonList(new LayerCoveringRange(layer)));

		SortedMap<LmnlRange, List<LmnlAnnotation>> msIndex = new OverlapIndexer(msPredicate).apply(ranges);
		for (LmnlRange segment : msIndex.keySet()) {
			for (LmnlAnnotation milestone : msIndex.get(segment)) {
				if (milestone instanceof LayerCoveringRange) {
					continue;
				}
				if (milestone.address().start == segment.start) {
					// FIXME: factory pattern!
					LmnlAnnotation element = new DefaultLmnlAnnotation(LmnlDocument.LMNL_NS_URI, "lmnl", elementName, null, new LmnlRange(segment));
					layer.add(element);
					for (LmnlAnnotation annotation : milestone) {
						element.add(annotation);
					}
					layer.remove(milestone);
				}
			}
		}
	}

	private void convertSpanningElements(LmnlLayer layer) {
		final Map<URI, LmnlAnnotation> ids = new HashMap<URI, LmnlAnnotation>();

		// collecting and indexing of identifyable ranges
		layer.visit(new LmnlLayer.Visitor() {

			@Override
			public void visit(LmnlLayer layer) {
				if (layer instanceof LmnlAnnotation && layer.getId() != null) {
					ids.put(layer.getId(), (LmnlAnnotation) layer);
				}

			}
		});

		// transformation of spanning elements
		layer.visit(new LmnlLayer.Visitor() {

			@Override
			public void visit(LmnlLayer layer) {
				if (!(layer instanceof LmnlAnnotation)) {
					return;
				}
				LmnlAnnotation range = (LmnlAnnotation) layer;
				String localName = range.getLocalName();
				if (!localName.endsWith("Span") && !localName.equals("index")) {
					return;
				}

				try {
					LmnlAnnotation to = Iterables.find(range.getAnnotations(), new Predicate<LmnlAnnotation>() {

						@Override
						public boolean apply(LmnlAnnotation input) {
							return "spanTo".equals(input.getLocalName()) && (input.getText() != null);
						}
					});

					LmnlAnnotation end = ids.get(new URI(to.getText()));
					if (end == null) {
						return;
					}

					if (localName.endsWith("Span")) {
						range.setLocalName(localName.substring(0, localName.length() - 4));
					}
					range.address().end = end.address().start;
					range.remove(to);

				} catch (NoSuchElementException e) {
				} catch (URISyntaxException e) {
				}
			}
		});
	}

	private static class TeiElementPredicate implements Predicate<LmnlAnnotation> {

		private String localName;

		private TeiElementPredicate(String localName) {
			this.localName = localName;
		}

		@Override
		public boolean apply(LmnlAnnotation input) {
			if (!TEI_NS.equals(input.getNamespace())) {
				return false;
			}

			return localName.equals(input.getLocalName());
		}

	}

	private static class LayerCoveringRange extends AbstractLmnlLayer implements LmnlAnnotation {
		private final LmnlLayer layer;
		private final LmnlRange rangeAddress;

		private LayerCoveringRange(LmnlLayer layer) {
			super(layer.getUri(), layer.getPrefix(), layer.getLocalName(), null);
			this.layer = layer;
			this.rangeAddress = new LmnlRange(0, layer.getText().length());
		}

		@Override
		public LmnlRange address() {
			return rangeAddress;
		}

		@Override
		public String getSegmentText() {
			return layer.getText();
		}
	}
}
