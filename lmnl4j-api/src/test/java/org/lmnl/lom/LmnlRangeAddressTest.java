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

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests operations on range addresses like offset manipulation.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class LmnlRangeAddressTest {

	/**
	 * Tests generation of ranges relative to another.
	 */
	@Test
	public void relativeRanges() {
		Assert.assertEquals("Prefix #1", new LmnlRangeAddress(0, 20), new LmnlRangeAddress(0, 20).relativeTo(new LmnlRangeAddress(0, 40)));
		Assert.assertEquals("Prefix #2", new LmnlRangeAddress(0, 10), new LmnlRangeAddress(10, 20).relativeTo(new LmnlRangeAddress(10, 40)));

		Assert.assertEquals("Suffix #1", new LmnlRangeAddress(10, 20), new LmnlRangeAddress(10, 20).relativeTo(new LmnlRangeAddress(0, 20)));
		Assert.assertEquals("Suffix #2", new LmnlRangeAddress(10, 30), new LmnlRangeAddress(20, 40).relativeTo(new LmnlRangeAddress(10, 40)));

		Assert.assertEquals("Middle #1", new LmnlRangeAddress(10, 20), new LmnlRangeAddress(10, 20).relativeTo(new LmnlRangeAddress(0, 30)));
		Assert.assertEquals("Middle #2", new LmnlRangeAddress(10, 30), new LmnlRangeAddress(20, 40).relativeTo(new LmnlRangeAddress(10, 50)));
		Assert.assertEquals("Middle #3", new LmnlRangeAddress(5, 15), new LmnlRangeAddress(25, 35).relativeTo(new LmnlRangeAddress(20, 40)));

		Assert.assertEquals("Overlap #1", new LmnlRangeAddress(0, 5), new LmnlRangeAddress(10, 20).relativeTo(new LmnlRangeAddress(15, 20)));
		Assert.assertEquals("Overlap #2", new LmnlRangeAddress(0, 5), new LmnlRangeAddress(15, 30).relativeTo(new LmnlRangeAddress(15, 20)));
		Assert.assertEquals("Overlap #3", new LmnlRangeAddress(0, 10), new LmnlRangeAddress(10, 30).relativeTo(new LmnlRangeAddress(15, 25)));
	}

	/**
	 * Tests substraction of ranges (for later removal of text segments).
	 */
	@Test
	public void substraction() {
		Assert.assertEquals("Predecessor #1", new LmnlRangeAddress(0, 20), new LmnlRangeAddress(0, 20).substract(new LmnlRangeAddress(20, 40)));
		Assert.assertEquals("Predecessor #2", new LmnlRangeAddress(0, 20), new LmnlRangeAddress(0, 20).substract(new LmnlRangeAddress(21, 40)));
		Assert.assertEquals("Successor #1", new LmnlRangeAddress(0, 10), new LmnlRangeAddress(10, 20).substract(new LmnlRangeAddress(0, 10)));
		Assert.assertEquals("Successor #2", new LmnlRangeAddress(1, 11), new LmnlRangeAddress(10, 20).substract(new LmnlRangeAddress(0, 9)));

		Assert.assertEquals("Start deleted", new LmnlRangeAddress(0, 10), new LmnlRangeAddress(0, 20).substract(new LmnlRangeAddress(0, 10)));
		Assert.assertEquals("End deleted", new LmnlRangeAddress(0, 10), new LmnlRangeAddress(0, 20).substract(new LmnlRangeAddress(10, 20)));
		Assert.assertEquals("Middle deleted", new LmnlRangeAddress(0, 10), new LmnlRangeAddress(0, 20).substract(new LmnlRangeAddress(5, 15)));
		Assert.assertEquals("End overlap deleted", new LmnlRangeAddress(0, 10), new LmnlRangeAddress(0, 20).substract(new LmnlRangeAddress(10, 30)));
		Assert.assertEquals("Start overlap deleted", new LmnlRangeAddress(5, 10), new LmnlRangeAddress(10, 20).substract(new LmnlRangeAddress(5, 15)));

		Assert.assertEquals("Overlap", new LmnlRangeAddress(0, 5), new LmnlRangeAddress(0, 10).substract(new LmnlRangeAddress(5, 26)));
		Assert.assertEquals("Full substraction", new LmnlRangeAddress(0, 0), new LmnlRangeAddress(0, 20).substract(new LmnlRangeAddress(0, 20)));

		try {

			Assert.fail("Enclosing substraction: " + new LmnlRangeAddress(0, 20).substract(new LmnlRangeAddress(0, 40)));
		} catch (IllegalArgumentException e) {
		}

		try {
			Assert.fail("Strictly enclosing substraction: " + new LmnlRangeAddress(10, 20).substract(new LmnlRangeAddress(0, 40)));
		} catch (IllegalArgumentException e) {
		}

	}
}
