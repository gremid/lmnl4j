package org.lmnl.event;

import org.junit.Test;
import org.lmnl.AbstractXmlSourcedTest;
import org.lmnl.LmnlAnnotation;
import org.lmnl.LmnlDocument;
import org.lmnl.LmnlRange;
import org.lmnl.xml.XmlElementAnnotation;

import com.google.common.collect.Ordering;

public class LmnlEventGeneratorTest extends AbstractXmlSourcedTest {

	@Test
	public void serializeWithRangeOrder() throws LmnlEventHandlerException {
		new LmnlEventGenerator().generate(document("george-algabal-tei.xml"), DEBUG_HANDLER);
	}

	@Test
	public void serializeWithXmlNodeAddressOrder() throws LmnlEventHandlerException {
		new LmnlEventGenerator().generate(document("george-algabal-tei.xml"), DEBUG_HANDLER, new Ordering<LmnlAnnotation>() {

			@Override
			public int compare(LmnlAnnotation o1, LmnlAnnotation o2) {
				if (o1 instanceof XmlElementAnnotation && o2 instanceof XmlElementAnnotation) {
					return ((XmlElementAnnotation)o1).getXmlNodeAddress().compareTo(((XmlElementAnnotation)o2).getXmlNodeAddress());
				}

				return o1.address().compareTo(o2.address());
			}
		});
	}

	private final LmnlEventHandler DEBUG_HANDLER = new LmnlEventHandler() {
		private int depth = 0;

		@Override
		public void startDocument(LmnlDocument document) {
			printDebugMessage(indent() + String.format("DocumentStart[%s{%s}]", document.getQName(),// 
					(document.getId() == null ? "" : document.getId())));
			depth++;
		}

		@Override
		public void startAnnotation(LmnlAnnotation annotation) {
			LmnlRange address = annotation.address();
			printDebugMessage(indent() + String.format("AnnotationStart[%s{%s}]: [%d,%d]", annotation.getQName(),// 
					(annotation.getId() == null ? "" : annotation.getId()), address.start, address.end));
			depth++;
		}

		@Override
		public void endDocument(LmnlDocument document) {
			depth--;
			printDebugMessage(indent() + String.format("DocumentEnd[%s{%s}]", document.getQName(),//
					(document.getId() == null ? "" : document.getId())));
		}

		@Override
		public void endAnnotation(LmnlAnnotation annotation) {
			depth--;
			printDebugMessage(indent() + String.format("AnnotationEnd[%s{%s}]", annotation.getQName(),//
					(annotation.getId() == null ? "" : annotation.getId())));
		}

		private String indent() {
			StringBuilder indent = new StringBuilder(depth);
			for (int dc = 0; dc < depth; dc++) {
				indent.append("\t");
			}
			return indent.toString();
		}
	};
}
