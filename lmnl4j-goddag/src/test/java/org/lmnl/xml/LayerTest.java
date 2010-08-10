package org.lmnl.xml;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.lmnl.AnnotationNode;
import org.neo4j.helpers.Predicate;

public class LayerTest extends XmlTest {
	@Test
	public void extractLayer() throws Exception {
		Document document = documents.get(0);

		XmlAnnotationNode text = document.selectSingleNode("//*[local-name(.) = 'text']");
		
		Document layer = nodeFactory.createNode(Document.class, AnnotationNode.SELF_OWNED);
		db.getReferenceNode().createRelationshipTo(layer.getUnderlyingNode(), TEST_REL);
		text.multiply(new MirrorPredicate(), layer);

		print(text);
		print(layer);

	}

	private static class MirrorPredicate implements Predicate<AnnotationNode> {
		private final Set<String> NAMES = new HashSet<String>(Arrays.asList(new String[] { "text", "ab", "seg", "l" }));

		@Override
		public boolean accept(AnnotationNode item) {

			if (item instanceof Text) {
				return true;
			}
			if (item instanceof Element && NAMES.contains(((Element) item).getName())) {
				return true;
			}
			if (item instanceof Attribute && accept(item.getParentNode())) {
				return true;
			}
			return false;
		}
	};

}
