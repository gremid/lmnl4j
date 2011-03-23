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

package org.lmnl;


/**
 * Layer of annotations applying to a marked-up text.
 * 
 * <p/>
 * 
 * Layers are isomorphic to {@link Document documents}, recursively
 * organized in a hierarchy and can either contain the marked up text themselves
 * or implicitely refer to a text of a lower layer they are attached to.
 * 
 * <p/>
 * 
 * Layers are not used by themselves to mark up a text; rather they define the
 * <b>hierarchical/ layered structure</b> of a LOM and provide <b>the markup's
 * subject</b>, the text. The most immediate and common entities derived from
 * this class, that actually comprise the markup, are documents, annotations and
 * ranges.
 * 
 * @see Document
 * @see Annotation
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public interface Layer {

	/**
	 * The document, this layer ultimately belongs to.
	 * 
	 * @return the document found by walking up the hierarchy of owning
	 *         layers or <code>null</code> in case the layer is not attached
	 *         to any document
	 */
	Document getDocument();

	/**
	 * The layer owning this one.
	 * 
	 * <p/>
	 * 
	 * Layers, as they are owned by other layers, form a hierarchy, that is
	 * used to let higher layers inherit properties from lower layers, most
	 * importantly the {@link #text() text} being marked up, but also
	 * administrative ones like {@link #getNamespaceContext() namespace
	 * mappings} for example.
	 * 
	 * @return the owner of this layer, most probably a {@link Document
	 *         document} or an {@link Annotation annotation}
	 */
	Layer getOwner();

	/**
	 * Name prefix of this layer, referring to the namespace it is in.
	 * 
	 * @return a prefix string, corresponding to the same <a
	 *         href="http://www.w3.org/TR/xml-names/"
	 *         title="Namespaces in XML">concept in XML</a>; may be the
	 *         empty string
	 * 
	 * @see #getNamespace()
	 */
	QName getName();
}