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

/**
 * A range annotation marking up a specific segment of text in its owning layer.
 * 
 * <p/>
 * 
 * While annotations refer to their owning layer in its entirety, range
 * annotations target parts of the text in the annotated layer. Think for
 * example of pure annotations as XML attributes annotating XML elements and of
 * range annotations as XML elements annotating portions of text.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public interface LmnlRange extends LmnlAnnotation {
	/**
	 * The segment/ range of text being annotated.
	 * 
	 * @return a value object adressing the text segment, that is annotated
	 *         by this range annotation
	 */
	LmnlRangeAddress address();
	
	/**
	 * The actual text being annotated by this range.
	 * 
	 * @return the textual contents of the segment
	 */
	String getSegmentText();
}