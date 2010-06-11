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
 * An annotation representing markup in a LOM.
 * 
 * <p/>
 * 
 * Currently this is just a marker interface, which distinguishes annotation
 * layers from the {@link LmnlDocument document layer}, but does not add any
 * annotation-specific functionality. Annotations only refer to their owning
 * layer, so they annotate it in its entirety. For markup, that targets a
 * specific segment of text in the owning layer, use {@link LmnlRange ranges}.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public interface LmnlAnnotation extends LmnlLayer {

}
