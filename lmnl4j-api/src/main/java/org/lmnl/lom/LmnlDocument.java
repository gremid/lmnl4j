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

/**
 * A document, the natural base layer of a LMNL object model (LOM).
 * 
 * <p/>
 * 
 * Documents form the lowest layer of LOMs, therefore they are necessarily
 * layers containing {@link LmnlLayer#getText() text}.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public interface LmnlDocument extends LmnlLayer {
	/**
	 * The URI of this document.
	 * 
	 * <p/>
	 * 
	 * A document's URI is used for example to create URIs of descendant
	 * layers by resolving their respective {@link LmnlLayer#getId()
	 * identifiers} against it.
	 * 
	 * @return the (mandatory) URI of a document
	 * @see <a href="http://www.w3.org/TR/xmlbase/" title="XML Base">XML
	 *      Base</a>
	 * 
	 */
	URI getBase();

	/**
	 * The LMNL namespace, mainly used as a default.
	 */
	final URI LMNL_NS_URI = URI.create("http://lmnl.net/namespaces/lmnl");
}