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
import java.util.Map;

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
	 * Registered mappings from name prefixes to namespace URIs in this
	 * layer.
	 * 
	 * <p/>
	 * 
	 * In a LOM, namespace mapping are aggregated at the lowest layer with
	 * higher layers delegating namespace handling to their owner. This is
	 * achieved by merging namespace registers upon addition of layer to
	 * others.
	 * 
	 * @return a map of registered namespaces, either originating from the
	 *         layer itself, if it happens to be the lowest, or from a layer
	 *         up the {@link #getOwner() chain} of owners.
	 * 
	 * @see #add(LmnlAnnotation)
	 */
	Map<String, URI> getNamespaceContext();

	/**
	 * Assigns a map of registered namespaces to this layer.
	 * 
	 * <p/>
	 * 
	 * As namespace handling is handled internally to a large extent during
	 * {@link #add(LmnlAnnotation) LOM construction}, for example by
	 * propagating mappings up the layer hierarchy, this method should
	 * normally not be called by users.
	 * 
	 * @param context
	 *                a map of namespaces registered in this layer and its
	 *                descendants or <code>null</code> in case the namespace
	 *                context of this layer shall be removed and further
	 *                lookups be delegated to its owner
	 */
	void setNamespaceContext(Map<String, URI> context);

	/**
	 * The LMNL namespace, mainly used as a default.
	 */
	final URI LMNL_NS_URI = URI.create("http://lmnl.net/namespaces/lmnl");
}