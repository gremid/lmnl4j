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
import org.lmnl.AbstractXMLTest;
import org.lmnl.Annotation;
import org.lmnl.Document;
import org.lmnl.Range;
import org.lmnl.xml.XMLUtils;
import org.lmnl.xml.XMLElement;

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
public class DefaultLayerTest extends AbstractXMLTest {
	/**
	 * Extracts a subset of annotations from a test document by using
	 * {@link Predicate predicates}.
	 */
	@Test
	public void extractViews() {
		Document d = document("george-algabal-tei.xml");

		Annotation drama = d.add("test", "drama", null, new Range(0, d.getText().length()), XMLElement.class);
		for (XMLElement a : Iterables.filter(Iterables.filter(d, XMLElement.class), DRAMA_ANNOTATIONS)) {
			XMLUtils.copy(a, drama);
			
		}

		Assert.assertFalse("Extracted dramatic text structure", Iterables.isEmpty(drama));

		printDebugMessage(drama);
		final Annotation spans = d.add("test", "spans", null, new Range(0, d.getText().length()), Annotation.class);
		for (XMLElement a : Lists.newArrayList(Iterables.filter(Iterables.filter(d, XMLElement.class), SPANNING_ANNOTATIONS))) {
			XMLUtils.copy(a, spans);
		}
	}

	private static Predicate<XMLElement> DRAMA_ANNOTATIONS = new Predicate<XMLElement>() {
		Pattern dramaTags = Pattern.compile("(lg)|(l)|(stage)|(speaker)|(sp)");

		public boolean apply(XMLElement input) {
			return dramaTags.matcher(input.getLocalName()).matches();
		}
	};

	private static Predicate<XMLElement> SPANNING_ANNOTATIONS = new Predicate<XMLElement>() {
		Pattern spanningTags = Pattern.compile(".+Span$");

		public boolean apply(XMLElement input) {
			return spanningTags.matcher(input.getLocalName()).matches();
		}
	};
}
