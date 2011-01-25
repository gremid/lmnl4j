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

package org.lmnl.base;

import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Test;
import org.lmnl.AbstractXmlTest;
import org.lmnl.LmnlAnnotation;
import org.lmnl.LmnlDocument;
import org.lmnl.LmnlRange;
import org.lmnl.xml.LmnlXmlUtils;
import org.lmnl.xml.XmlElementAnnotation;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Tests layer functionality.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class DefaultLmnlLayerTest extends AbstractXmlTest {
	/**
	 * Extracts a subset of annotations from a test document by using
	 * {@link Predicate predicates}.
	 */
	@Test
	public void extractViews() {
		LmnlDocument d = document("george-algabal-tei.xml");

		LmnlAnnotation drama = d.add("test", "drama", null, new LmnlRange(0, d.getText().length()), XmlElementAnnotation.class);
		for (XmlElementAnnotation a : Iterables.filter(Iterables.filter(d, XmlElementAnnotation.class), DRAMA_ANNOTATIONS)) {
			LmnlXmlUtils.copy(a, drama);
			
		}

		Assert.assertFalse("Extracted dramatic text structure", Iterables.isEmpty(drama));

		printDebugMessage(drama);
		final LmnlAnnotation spans = d.add("test", "spans", null, new LmnlRange(0, d.getText().length()), LmnlAnnotation.class);
		for (XmlElementAnnotation a : Lists.newArrayList(Iterables.filter(Iterables.filter(d, XmlElementAnnotation.class), SPANNING_ANNOTATIONS))) {
			LmnlXmlUtils.copy(a, spans);
		}
	}

	private static Predicate<XmlElementAnnotation> DRAMA_ANNOTATIONS = new Predicate<XmlElementAnnotation>() {
		Pattern dramaTags = Pattern.compile("(lg)|(l)|(stage)|(speaker)|(sp)");

		public boolean apply(XmlElementAnnotation input) {
			return dramaTags.matcher(input.getLocalName()).matches();
		}
	};

	private static Predicate<XmlElementAnnotation> SPANNING_ANNOTATIONS = new Predicate<XmlElementAnnotation>() {
		Pattern spanningTags = Pattern.compile(".+Span$");

		public boolean apply(XmlElementAnnotation input) {
			return spanningTags.matcher(input.getLocalName()).matches();
		}
	};
}
