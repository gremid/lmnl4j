package org.lmnl.xml;

import org.lmnl.AnnotationNode;
import org.lmnl.AnnotationNodeFactory;
import org.lmnl.AnnotationTree;
import org.neo4j.graphdb.Node;

public class Text extends XmlAnnotationNode {
	public static final String NODE_TYPE = "xml:text";

	public Text(AnnotationNodeFactory nodeFactory, Node node, AnnotationNode root) {
		super(nodeFactory, node, root);
	}

	public String getContent() {
		return (String) getUnderlyingNode().getProperty("text");
	}

	public void setContent(String content) {
		getUnderlyingNode().setProperty("text", content);
	}

	@Override
	public void copyProperties(AnnotationNode other) {
		super.copyProperties(other);
		Text otherText = (Text) other;
		setContent(otherText.getContent());
	}

	@Override
	public String getTextContent() {
		return getContent();
	}

    public Text[] splitAfter(int position) {
        final String content = getContent();
        if (position < 0 || position >= (content.length() - 1)) {
            throw new IllegalArgumentException(Integer.toString(position));
        }
        
        final String left = content.substring(0, position + 1);
        final String right = content.substring(position + 1);
        Text[] result = new Text[2];
        
        for (AnnotationNode root: AnnotationTree.findAllTreeRoots(this)) {
            AnnotationNode treeNode = root.adopt(this);
            AnnotationNode treeParent = treeNode.getParentNode();
            if (treeParent != null) {
                Text leftText = nodeFactory.createNode(Text.class, root);
                leftText.setContent(left);
                treeParent.insert(leftText, treeNode);
                
                Text rightText = nodeFactory.createNode(Text.class, root);
                rightText.setContent(right);
                treeParent.insert(rightText, treeNode);
                
                treeParent.remove(treeNode, false);
                
                if (root.equals(getRoot())) {
                    result[0] = leftText;
                    result[1] = rightText;
                }
            }
        }

        return result;
    }
}
