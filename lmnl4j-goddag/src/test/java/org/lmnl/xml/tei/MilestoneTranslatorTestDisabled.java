package org.lmnl.xml.tei;

import org.junit.Test;
import org.lmnl.AnnotationNode;
import org.lmnl.xml.Document;
import org.lmnl.xml.Element;
import org.lmnl.xml.Text;
import org.lmnl.xml.XmlAnnotationNode;
import org.lmnl.xml.XmlTest;
import org.neo4j.helpers.Predicate;

public class MilestoneTranslatorTestDisabled extends XmlTest {

	@Test
	public void linebreaks() throws Exception {
		Document algabal = documents.get(1);
		XmlAnnotationNode text = (XmlAnnotationNode) TeiUtil.newXPathContext(algabal).selectSingleNode("//tei:text");

		Document lineLayer = nodeFactory.createNode(Document.class);
		db.getReferenceNode().createRelationshipTo(lineLayer.getUnderlyingNode(), TEST_REL);
		text.multiply(new Predicate<AnnotationNode>() {

			@Override
			public boolean accept(AnnotationNode item) {
				if (item instanceof Element) {
					return "text".equals(((Element) item).getName());
				}
				return (item instanceof Text);
			}
		}, lineLayer);

		new LineBreakTranslator().translate(algabal, (XmlAnnotationNode) TeiUtil.newXPathContext(lineLayer).selectSingleNode("tei:text"));

		print(lineLayer);
	}
}
