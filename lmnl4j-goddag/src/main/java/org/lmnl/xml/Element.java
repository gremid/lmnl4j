package org.lmnl.xml;

import static org.lmnl.xml.XmlAnnotationTree.ATTRIBUTE;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

import org.lmnl.AnnotationNode;
import org.lmnl.AnnotationNodeFactory;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.helpers.collection.IterableWrapper;

import com.google.common.collect.Iterables;

public class Element extends XmlNamedAnnotationNode {
	public static final String NODE_TYPE = "xml:element";

	public Element(AnnotationNodeFactory nodeFactory, Node node, AnnotationNode root) {
		super(nodeFactory, node, root);
	}

	public Iterable<Attribute> getAttributes() {
	    return new IterableWrapper<Attribute, Relationship>(getUnderlyingNode().getRelationships(ATTRIBUTE, OUTGOING)) {

            @Override
            protected Attribute underlyingObjectToObject(Relationship object) {
                return (Attribute) nodeFactory.wrapNode(object.getEndNode(), getRoot());
            }
        };
	}

    public void addAttribute(Attribute attr) {
        for (Attribute existing : Iterables.toArray(getAttributes(), Attribute.class)) {
            if (attr.getName().equals(existing.getName()) && attr.getNamespace().equals(existing.getNamespace())) {
                removeAttribute(existing);
            }
        }
        
        getUnderlyingNode().createRelationshipTo(attr.getUnderlyingNode(), ATTRIBUTE);
    }

    public void removeAttribute(Attribute attr) {
        if (!equals(attr.getElement())) {
            throw new IllegalArgumentException(attr.toString());
        }
        Node attrNode = attr.getUnderlyingNode();
        attrNode.getSingleRelationship(ATTRIBUTE, INCOMING).delete();
        attrNode.delete();
    }
    
    @Override
    protected void beforeRemoval() {
        super.beforeRemoval();
        for (Attribute attr : Iterables.toArray(getAttributes(), Attribute.class)) {
            removeAttribute(attr);
        }
    }
    
	@Override
	public String toString() {
		return "<" + getName() + "/> [" + getUnderlyingNode() + "]";
	}
}
