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

import org.junit.Test;
import org.lmnl.AbstractXmlSourcedTest;
import org.lmnl.lom.LmnlAnnotation;
import org.lmnl.lom.util.OverlapIndexer;

import com.google.common.base.Predicate;

/**
 * Tests overlap indexing on TEI documents.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class TeiOverlapIndexerTest extends AbstractXmlSourcedTest {

	/**
	 * Indexes a test document by segment (<code>&lt;seg/></code>).
	 */
	@Test
	public void indexBySegment() {
		printDebugMessage(new OverlapIndexer(new Predicate<LmnlAnnotation>() {

			@Override
			public boolean apply(LmnlAnnotation input) {
				return "seg".equals(input.getLocalName());
			}
		}).apply(document()));
	}

	/**
	 * Indexes a test document by page breaks (<code>&lt;pb/></code>).
	 */
	@Test
	public void indexByPageBreak() throws Exception {
		printDebugMessage(new OverlapIndexer(new Predicate<LmnlAnnotation>() {

			@Override
			public boolean apply(LmnlAnnotation input) {
				return "pb".equals(input.getLocalName());
			}
		}).apply(document("george-algabal-tei.xml")));
	}
}
