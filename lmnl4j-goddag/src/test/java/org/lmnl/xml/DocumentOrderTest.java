package org.lmnl.xml;

import org.junit.Test;
import org.lmnl.AnnotationNode;
import org.lmnl.xml.tei.TeiUtil;

public class DocumentOrderTest extends XmlTest {

	@Test
	public void walk() {
		for (AnnotationNode node : documents.get(1).walk()) {
			node.getClass();
		}
	}

	@Test
	public void precedingAxis() {
		for (AnnotationNode node : ((XmlAnnotationNode) TeiUtil.newXPathContext(documents.get(1)).selectSingleNode(
				"//tei:pb")).getPrecedingNodes()) {
			node.getClass();
		}
	}

	@Test
	public void followingAxis() {
		for (AnnotationNode node : ((XmlAnnotationNode) TeiUtil.newXPathContext(documents.get(1)).selectSingleNode(
				"//tei:pb")).getFollowingNodes()) {
			node.getClass();
		}
	}
}
