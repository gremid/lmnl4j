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

package org.lmnl.lom;

import java.net.URI;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Test;
import org.lmnl.AbstractXmlSourcedTest;
import org.lmnl.lom.base.DefaultLmnlAnnotation;
import org.lmnl.lom.base.DefaultLmnlDocument;

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
public class LmnlLayerTest extends AbstractXmlSourcedTest {
	/**
	 * Extracts a subset of annotations from a test document by using
	 * {@link Predicate predicates}.
	 */
	@Test
	public void extractViews() {
		DefaultLmnlDocument d = document("george-algabal-tei.xml");

		final LmnlAnnotation drama = new DefaultLmnlAnnotation(URI.create("urn:lmnl-test"), "test", "drama", null, d.getCoveringRange());
		d.add(drama);
		for (LmnlAnnotation a : Lists.newArrayList(Iterables.filter(d, DRAMA_ANNOTATIONS))) {
			drama.add(a);
		}

		Assert.assertTrue("Extracted dramatic text structure", drama.getAnnotations().size() > 0);

		final LmnlAnnotation spans = new DefaultLmnlAnnotation(URI.create("urn:lmnl-test"), "test", "spans", null, d.getCoveringRange());
		d.add(spans);
		for (LmnlAnnotation a : Lists.newArrayList(Iterables.filter(d, SPANNING_ANNOTATIONS))) {
			drama.add(a);
		}

	}

	private static Predicate<LmnlAnnotation> DRAMA_ANNOTATIONS = new Predicate<LmnlAnnotation>() {
		Pattern dramaTags = Pattern.compile("(lg)|(l)|(stage)|(speaker)|(sp)");

		@Override
		public boolean apply(LmnlAnnotation input) {
			return dramaTags.matcher(input.getLocalName()).matches();
		}
	};

	private static Predicate<LmnlAnnotation> SPANNING_ANNOTATIONS = new Predicate<LmnlAnnotation>() {
		Pattern spanningTags = Pattern.compile(".+Span$");

		@Override
		public boolean apply(LmnlAnnotation input) {
			return spanningTags.matcher(input.getLocalName()).matches();
		}
	};
}
