package org.lmnl;

public interface AnnotationRepository {

	Iterable<Annotation> getAnnotations(Layer layer);
	
	Iterable<Annotation> find(Layer layer, QName annotationName);

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
	Annotation add(Layer to, Annotation annotation);
	
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
	void remove(Layer from, Annotation annotation);
	
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
	void visit(Layer layer, Visitor visitor);
}
