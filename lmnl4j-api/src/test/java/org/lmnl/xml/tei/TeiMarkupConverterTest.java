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

import junit.framework.Assert;

import org.junit.Test;
import org.lmnl.AbstractXmlSourcedTest;
import org.lmnl.LmnlAnnotation;
import org.lmnl.base.DefaultLmnlDocument;
import org.lmnl.xml.tei.TeiMarkupConverter;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Tests the conversion of TEI-P5-specific markup like spanning elements or
 * milestones.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class TeiMarkupConverterTest extends AbstractXmlSourcedTest {

	/**
	 * Converts a TEI-P5-based document.
	 */
	@Test
	public void convertDocument() {
		DefaultLmnlDocument d = document("george-algabal-tei.xml");
		new TeiMarkupConverter().convert(d);
		printDebugMessage(d);
		Assert.assertTrue("<*b/> substitutes in document", Iterables.any(d, new Predicate<LmnlAnnotation>() {

			@Override
			public boolean apply(LmnlAnnotation input) {
				final String name = input.getLocalName();
				return name.equals("page") || name.equals("line") || name.equals("column");
			}
		}));
		Assert.assertFalse("No spans in document", Iterables.any(d.getAnnotations(), new Predicate<LmnlAnnotation>() {

			@Override
			public boolean apply(LmnlAnnotation input) {
				return input.getLocalName().endsWith("Span");
			}
		}));
	}
}
