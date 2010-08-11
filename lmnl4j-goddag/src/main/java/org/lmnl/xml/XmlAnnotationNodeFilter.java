package org.lmnl.xml;

import org.lmnl.AnnotationNode;
import org.neo4j.helpers.Predicate;
import org.neo4j.helpers.collection.FilteringIterable;
import org.neo4j.helpers.collection.IterableWrapper;

public class XmlAnnotationNodeFilter extends FilteringIterable<AnnotationNode> {

	public XmlAnnotationNodeFilter(Iterable<AnnotationNode> source) {
		super(source, new Predicate<AnnotationNode>() {
			@Override
			public boolean accept(AnnotationNode item) {
				return (item instanceof XmlAnnotationNode);
			}
		});
	}

	public static Iterable<XmlAnnotationNode> filter(Iterable<AnnotationNode> source) {
		return new IterableWrapper<XmlAnnotationNode, AnnotationNode>(new XmlAnnotationNodeFilter(source)) {

			@Override
			protected XmlAnnotationNode underlyingObjectToObject(AnnotationNode object) {
				return (XmlAnnotationNode) object;
			}
		};
	}
}