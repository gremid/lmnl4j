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

package org.lmnl.lom.util;

import org.junit.Test;
import org.lmnl.AbstractDefaultLmnlDocumentTest;
import org.lmnl.lom.util.OverlapIndexer;

/**
 * Tests the calculation of overlap indizes.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class OverlapIndexerTest extends AbstractDefaultLmnlDocumentTest {

	/**
	 * Indexes a very simple document.
	 */
	@Test
	public void indexsegmentSimpleDocument() {
		addTestRange("a", 0, 2);
		addTestRange("b", 1, 4);
		addTestRange("c", 0, 1);
		addTestRange("d", 0, 6);
		addTestRange("e", 2, 3);

		printDebugMessage(document);
		printDebugMessage(new OverlapIndexer().apply(document));
	}

	@Override
	protected String documentText() {
		return "abcdef";
	}
}
