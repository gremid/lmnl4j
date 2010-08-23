package org.lmnl.xml;

import static org.lmnl.xml.XmlAnnotationTree.ATTRIBUTE;
import static org.neo4j.graphdb.Direction.INCOMING;

import org.lmnl.AnnotationNode;
import org.lmnl.AnnotationNodeFactory;
import org.neo4j.graphdb.Node;

public class Attribute extends XmlNamedAnnotationNode {
    public static final String NODE_TYPE = "xml:attribute";

    public Attribute(AnnotationNodeFactory nodeFactory, Node node, AnnotationNode root) {
        super(nodeFactory, node, root);
    }

    public Element getElement() {
        return (Element) nodeFactory.wrapNode(getUnderlyingNode().getSingleRelationship(ATTRIBUTE, INCOMING).getStartNode(),
                getRoot());
    }

    public void setValue(String value) {
        getUnderlyingNode().setProperty("value", value);
    }

    public String getValue() {
        return (String) getUnderlyingNode().getProperty("value");
    }

    @Override
    public String getTextContent() {
        return getValue();
    }

    @Override
    public String toString() {
        return "@" + getName() + " [" + getUnderlyingNode() + "]";
    }

    @Override
    public void copyProperties(AnnotationNode other) {
        super.copyProperties(other);
        Attribute otherAttribute = (Attribute) other;
        setValue(otherAttribute.getValue());
    }
}
