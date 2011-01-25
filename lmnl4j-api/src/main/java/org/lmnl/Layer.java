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

import java.net.URI;

import org.codehaus.jackson.JsonGenerator;

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
public interface Layer extends Iterable<Annotation> {

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
	 * importantly the {@link #getText() text} being marked up, but also
	 * administrative ones like {@link #getNamespaceContext() namespace
	 * mappings} for example.
	 * 
	 * @return the owner of this layer, most probably a {@link Document
	 *         document} or an {@link Annotation annotation}
	 */
	Layer getOwner();

	/**
	 * A textual key, uniquely identifying the layer within a document.
	 * 
	 * <p/>
	 * 
	 * May provide for means of cross-referencing annotations, especially in
	 * the context of {@link #serialize(JsonGenerator) serialized} LOMs, and
	 * for naming layers by combining the {@link Document#getBase() base
	 * URI} of the owning document and this layer's id.
	 * 
	 * <p/>
	 * 
	 * Note: While transforming from XML documents to LOMs, mapping
	 * <code>xml:id</code> directly to this property might be useful.
	 * 
	 * @return the key or <code>null</code> if none has been assigned
	 * 
	 * @see #getUri()
	 */
	URI getId();

	/**
	 * Assigns a unique textual key identifying this layer.
	 * 
	 * @param id
	 *                the key value, conforming to the syntax of {@link URI}
	 *                fragment identifiers
	 * 
	 * @see #getUri()
	 */
	void setId(URI id);

	/**
	 * Constructs the URI of this layer.
	 * 
	 * <p/>
	 * 
	 * The URI is constructed by resolving the {@link #getId() identifier}
	 * of this layer as a fragment identifier against the
	 * {@link Document#getBase() base URI} of the owning document.
	 * 
	 * @return the URI or <code>null</code> in case, no identifier has been
	 *         assigned to this layer
	 * @throws IllegalStateException
	 *                 in case the layer is not associated with a document
	 */
	URI getUri();

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
	String getPrefix();

	/**
	 * The name of this layer, which is local to its namespace context.
	 * 
	 * @return the local name of the layer, corresponding to the same <a
	 *         href="http://www.w3.org/TR/xml-names/"
	 *         title="Namespaces in XML">concept in XML</a>
	 */
	String getLocalName();

	/**
	 * Looks up and returns the namespace of this layer.
	 * 
	 * @return the URI of this layer's namespace as determined by the
	 *         {@link #getNamespaceContext() namespace context} or
	 *         <code>null</code> if the layer's prefix does not map to a
	 *         registered namespace
	 */
	URI getNamespace();

	
	/**
	 * Delivers the qualified name of this layer.
	 * 
	 * @return the qualified name by combining the (possibly empty)
	 *         namespace prefix and the local name
	 * 
	 * @see #getPrefix()
	 * @see #getLocalName()
	 */
	String getQName();

	/**
	 * Returns the text, the markup of this layer applies to.
	 * 
	 * 
	 * @return the text of this layer, if it contains some, the text of this
	 *         layer's {@link #getOwner() owner} otherwise
	 */
	String getText();

	void setText(String text);
	
	/**
	 * Does this annotation layer has a text of its own, or does it annotate
	 * the text of its owning layer.
	 * 
	 * @return <code>true</code> or <code>false</code>
	 */
	boolean hasText();

	/**
	 * Registers an annotation with this layer as its child.
	 * 
	 * <p/>
	 * 
	 * Should the provided annotation already be owned by another layer, it
	 * is {@link #remove(Annotation) removed} from it first. Also in the
	 * course of adding the annotation, its {@link #getNamespaceContext()
	 * namespace context}, should there be one, is merged with the context
	 * of this layer.
	 * 
	 * @param annotation
	 *                the annotation to be owned by this layer
	 * @return the added annotation
	 */
	<T extends Annotation> T add(String prefix, String localName, String text, Range address, Class<T> type);

	<T extends Annotation> T add(T annotation, Class<T> type);
	
	/**
	 * Detaches/ Removes an annotation from this layer.
	 * 
	 * <p/>
	 * 
	 * Should the annotation not be owned by this layer, it is return
	 * unmodified. Otherwise the namespace context of this layer is copied
	 * to the annotation, its owner set to <code>null</code> and then
	 * returned in this state, so it can be added to another layer if
	 * desired.
	 * 
	 * @param annotation
	 *                the annotation to be removed
	 */
	void remove(Annotation annotation);

	<T extends Annotation> Iterable<T> select(Class<T> annotationType);
	
	/**
	 * Recursively visits all descendants of this layer and calls back the
	 * visitor.
	 * 
	 * <p/>
	 * 
	 * The traversal of the layer hierarchy is breadth-first and excludes
	 * the called layer itself.
	 * 
	 * @param visitor
	 *                the object called back for every traversed layer
	 */
	void visit(Visitor visitor);

	/**
	 * Callback interface for traversing layers.
	 * 
	 * @see Layer#visit(Visitor)
	 * 
	 * @author <a href="http://gregor.middell.net/"
	 *         title="Homepage of Gregor Middell">Gregor Middell</a>
	 * 
	 */
	interface Visitor {

		/**
		 * Called for every layer traversed.
		 * 
		 * @param layer
		 *                the layer (annotation, range etc.) being
		 *                visited via some traversal algorithm
		 */
		void visit(Layer layer);
	}
}