package org.lmnl;

/**
 * Callback interface for traversing layers.
 * 
 * @see Layer#visit(Visitor)
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public interface Visitor {

	/**
	 * Called for every layer traversed.
	 * 
	 * @param layer
	 *                the layer (annotation, range etc.) being
	 *                visited via some traversal algorithm
	 */
	void visit(Layer layer);
}