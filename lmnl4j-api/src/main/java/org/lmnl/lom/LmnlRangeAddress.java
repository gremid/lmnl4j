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

import com.google.common.base.Objects;

/**
 * Adresses a text segment, for example a segment, that is annotated by some
 * {@link LmnlRange range annotation}.
 * 
 * <p/>
 * 
 * Segments are adressed by start and end offsets of the characters forming the
 * boundaries of a segment. The character pointed to by the start offset is
 * included in the segment, while the character addressed by the end offset is
 * the first excluded from it.
 * 
 * <p/>
 * 
 * Offsets are counted from zero and are located in the <i>gaps</i> between
 * characters:
 * 
 * <pre>
 *   a   b   c   d   e  
 * 0 | 1 | 2 | 3 | 4 | 5
 * </pre>
 * 
 * In the given example, the substring "bcd" would be adressed by the segment
 * <code>[1, 4]</code>, the whole string by the segment <code>[0, 5]</code>.
 * Note that the difference between the offsets equals the length of the segment
 * and that "empty" segments pointing in the gaps between characters are valid.
 * So for example to point to the gap between "d" and "e", the corresponding
 * empty segment's address would be <code>[4, 4]</code>.
 * 
 * <p/>
 * 
 * Apart from encapsulating the offset values denoting the segment, objects of
 * this class also have methods to apply <a href=
 * "http://www.mind-to-mind.com/library/papers/ara/core-range-algebra-03-2002.pdf"
 * title="Nicol: Core Range Algebra (PDF)">Gavin Nicols' Core Range Algebra</a>.
 * These methods like {@link #encloses(LmnlRangeAddress)} or
 * {@link #hasOverlapWith(LmnlRangeAddress)} define relationships between text
 * segments, which can be used for example to filter sets of range annotations.
 * 
 * @see CharSequence#subSequence(int, int)
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class LmnlRangeAddress implements Comparable<LmnlRangeAddress> {
	public static final LmnlRangeAddress NULL = new LmnlRangeAddress(0, 0);

	/** The start offset of the segment (counted from zero, inclusive). */
	public int start;

	/** The end offset of the segment (counted from zero, exclusive). */
	public int end;

	/**
	 * Creates a text segment address.
	 * 
	 * @param start
	 *                start offset
	 * @param end
	 *                end offset
	 * @throws IllegalArgumentException
	 *                 if <code>start</code> or <code>end</code> or lower
	 *                 than zero, or if <code>start</code> is greather than
	 *                 <code>end</code>
	 */
	public LmnlRangeAddress(int start, int end) {
		if (start < 0 || end < 0 || start > end) {
			throw new IllegalArgumentException(toString(start, end));
		}
		this.start = start;
		this.end = end;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param b
	 *                the segment address to be copied
	 */
	public LmnlRangeAddress(LmnlRangeAddress b) {
		this(b.start, b.end);
	}

	/**
	 * The length of the adressed segment.
	 * 
	 * @return the length (difference between start and end offset)
	 */
	public int length() {
		return end - start;
	}

	/**
	 * Applies the adress to a string, returning the addressed segment.
	 * 
	 * @param text
	 *                the string, whose segment is addressed
	 * @return the subsequence/segment of the text
	 * @see String#substring(int, int)
	 */
	public String applyTo(String text) {
		return text.substring(start, end);
	}

	/**
	 * <i>a.start &lt;= b.start and a.end &gt;= b.end</i>
	 * 
	 * @param b
	 *                b range
	 * @return <code>true</code>/<code>false</code>
	 */
	public boolean encloses(LmnlRangeAddress b) {
		return (start <= b.start) && (end >= b.end);
	}

	/**
	 * <i>a.start = b.start and a.end &gt; b.end</i>
	 * 
	 * @param b
	 *                b range
	 * @return <code>true</code>/<code>false</code>
	 */
	public boolean enclosesWithSuffix(LmnlRangeAddress b) {
		return (start == b.start) && (end > b.end);
	}

	/**
	 * <i>a.start &lt; b.start and a.end = b.end</i>
	 * 
	 * @param b
	 *                b range
	 * @return <code>true</code>/<code>false</code>
	 */
	public boolean enclosesWithPrefix(LmnlRangeAddress b) {
		return (start < b.start) && (end == b.end);
	}

	/**
	 * <i>(a <> b) and a.start &gt; b.start and a.end &lt;= b.end</i>
	 * 
	 * @param b
	 *                b range
	 * @return <code>true</code>/<code>false</code>
	 */
	public boolean fitsWithin(LmnlRangeAddress b) {
		return !equals(b) && (start >= b.start) && (end <= b.end);
	}

	/**
	 * <i>overlap(a, b) &gt; 0</i>
	 * 
	 * @param b
	 *                b range
	 * @return <code>true</code>/<code>false</code>
	 */
	public boolean hasOverlapWith(LmnlRangeAddress b) {
		return overlapWith(b) > 0;
	}

	/**
	 * Yields the overlapping segment of this and another segment.
	 * 
	 * @param b
	 *                another segment
	 * @return <i>[max(a.start, b.start), min(a.end, b.end)]</i>
	 */
	public LmnlRangeAddress intersectionWith(LmnlRangeAddress b) {
		return new LmnlRangeAddress(Math.max(start, b.start), Math.min(end, b.end));
	}

	/**
	 * <i>min(a.end, b.end) - max(a.start, b.start)</i>
	 * 
	 * @param b
	 *                b range
	 * @return length of overlap
	 */
	public int overlapWith(LmnlRangeAddress b) {
		return (Math.min(end, b.end) - Math.max(start, b.start));
	}

	/**
	 * <i>b.start &gt;= a.end</i>
	 * 
	 * @param b
	 *                b range
	 * @return <code>true</code>/<code>false</code>
	 */
	public boolean precedes(LmnlRangeAddress b) {
		return b.start >= end;
	}

	/**
	 * <i>a.start &gt;= (b.end - 1)</i>
	 * 
	 * @param b
	 *                b range
	 * @return <code>true</code>/<code>false</code>
	 */
	public boolean follows(LmnlRangeAddress b) {
		return (start >= (b.end - 1));
	}

	/**
	 * Orders segments, first by start offset, then by the reverse order of
	 * the end offsets.
	 * 
	 * @see Comparable#compareTo(Object)
	 */
	@Override
	public int compareTo(LmnlRangeAddress o) {
		return (start == o.start ? o.end - end : start - o.start);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(start, end);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof LmnlRangeAddress)) {
			return super.equals(obj);
		}

		LmnlRangeAddress b = (LmnlRangeAddress) obj;
		return (this.start == b.start) && (this.end == b.end);
	}

	/**
	 * Creates a string representation of an offset pair.
	 * 
	 * @param start
	 *                start offset
	 * @param end
	 *                end offset
	 * @return string representation
	 */
	public static String toString(int start, int end) {
		return "[" + start + ", " + end + "]";
	}

	@Override
	public String toString() {
		return toString(start, end);
	}

	public LmnlRangeAddress substract(LmnlRangeAddress subtrahend) {
		if (end <= subtrahend.start) {
			// predecessor of deleted segment
			return new LmnlRangeAddress(start, end);
		}

		int length = subtrahend.end - subtrahend.start;

		if (start >= subtrahend.end) {
			// successor of deleted range
			return new LmnlRangeAddress(start - length, end - length);
		}

		int overlap = overlapWith(subtrahend);
		int start = (this.start < subtrahend.start ? this.start : this.start - (length - overlap));
		int end = (this.end >= subtrahend.end ? this.end - length : this.end - overlap);
		return new LmnlRangeAddress(start, end);
	}
}
