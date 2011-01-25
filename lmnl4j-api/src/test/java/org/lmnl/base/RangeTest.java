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

import org.junit.Assert;
import org.junit.Test;
import org.lmnl.Range;

/**
 * Tests operations on range addresses like offset manipulation.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class RangeTest {

	/**
	 * Tests generation of ranges relative to another.
	 */
	@Test
	public void relativeRanges() {
//		Assert.assertEquals("Prefix #1", new LmnlRangeAddress(0, 20), new LmnlRangeAddress(0, 20).relativeTo(new LmnlRangeAddress(0, 40)));
//		Assert.assertEquals("Prefix #2", new LmnlRangeAddress(0, 10), new LmnlRangeAddress(10, 20).relativeTo(new LmnlRangeAddress(10, 40)));
//
//		Assert.assertEquals("Suffix #1", new LmnlRangeAddress(10, 20), new LmnlRangeAddress(10, 20).relativeTo(new LmnlRangeAddress(0, 20)));
//		Assert.assertEquals("Suffix #2", new LmnlRangeAddress(10, 30), new LmnlRangeAddress(20, 40).relativeTo(new LmnlRangeAddress(10, 40)));
//
//		Assert.assertEquals("Middle #1", new LmnlRangeAddress(10, 20), new LmnlRangeAddress(10, 20).relativeTo(new LmnlRangeAddress(0, 30)));
//		Assert.assertEquals("Middle #2", new LmnlRangeAddress(10, 30), new LmnlRangeAddress(20, 40).relativeTo(new LmnlRangeAddress(10, 50)));
//		Assert.assertEquals("Middle #3", new LmnlRangeAddress(5, 15), new LmnlRangeAddress(25, 35).relativeTo(new LmnlRangeAddress(20, 40)));
//
//		Assert.assertEquals("Overlap #1", new LmnlRangeAddress(0, 5), new LmnlRangeAddress(10, 20).relativeTo(new LmnlRangeAddress(15, 20)));
//		Assert.assertEquals("Overlap #2", new LmnlRangeAddress(0, 5), new LmnlRangeAddress(15, 30).relativeTo(new LmnlRangeAddress(15, 20)));
//		Assert.assertEquals("Overlap #3", new LmnlRangeAddress(0, 10), new LmnlRangeAddress(10, 30).relativeTo(new LmnlRangeAddress(15, 25)));
	}

	/**
	 * Tests substraction of ranges (for later removal of text segments).
	 */
	@Test
	public void substraction() {
		Assert.assertEquals("Predecessor #1", new Range(0, 20), new Range(0, 20).substract(new Range(20, 40)));
		Assert.assertEquals("Predecessor #2", new Range(0, 20), new Range(0, 20).substract(new Range(21, 40)));
		Assert.assertEquals("Successor #1", new Range(0, 10), new Range(10, 20).substract(new Range(0, 10)));
		Assert.assertEquals("Successor #2", new Range(1, 11), new Range(10, 20).substract(new Range(0, 9)));

		Assert.assertEquals("Start deleted", new Range(0, 10), new Range(0, 20).substract(new Range(0, 10)));
		Assert.assertEquals("End deleted", new Range(0, 10), new Range(0, 20).substract(new Range(10, 20)));
		Assert.assertEquals("Middle deleted", new Range(0, 10), new Range(0, 20).substract(new Range(5, 15)));
		Assert.assertEquals("End overlap deleted", new Range(0, 10), new Range(0, 20).substract(new Range(10, 30)));
		Assert.assertEquals("Start overlap deleted", new Range(5, 10), new Range(10, 20).substract(new Range(5, 15)));

		Assert.assertEquals("Overlap", new Range(0, 5), new Range(0, 10).substract(new Range(5, 26)));
		Assert.assertEquals("Full substraction", new Range(0, 0), new Range(0, 20).substract(new Range(0, 20)));

		try {

			Assert.fail("Enclosing substraction: " + new Range(0, 20).substract(new Range(0, 40)));
		} catch (IllegalArgumentException e) {
		}

		try {
			Assert.fail("Strictly enclosing substraction: " + new Range(10, 20).substract(new Range(0, 40)));
		} catch (IllegalArgumentException e) {
		}

	}
}
