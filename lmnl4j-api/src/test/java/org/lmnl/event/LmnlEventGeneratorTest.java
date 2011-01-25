package org.lmnl.event;

import org.junit.Test;
import org.lmnl.AbstractXmlTest;
import org.lmnl.LmnlAnnotation;
import org.lmnl.LmnlDocument;
import org.lmnl.LmnlRange;
import org.lmnl.xml.XPathAddress;
import org.lmnl.xml.XmlElementAnnotation;

import com.google.common.collect.Ordering;

public class LmnlEventGeneratorTest extends AbstractXmlTest {

	@Test
	public void serializeWithRangeOrder() throws LmnlEventHandlerException {
		new LmnlEventGenerator().generate(document("george-algabal-tei.xml"), DEBUG_HANDLER);
	}

	@Test
	public void serializeWithXmlNodeAddressOrder() throws LmnlEventHandlerException {
		new LmnlEventGenerator().generate(document("george-algabal-tei.xml"), DEBUG_HANDLER,
				new Ordering<LmnlAnnotation>() {

					public int compare(LmnlAnnotation o1, LmnlAnnotation o2) {
						if (o1 instanceof XmlElementAnnotation && o2 instanceof XmlElementAnnotation) {
							XPathAddress xpath1 = ((XmlElementAnnotation) o1).getXPathAddress();
							XPathAddress xpath2 = ((XmlElementAnnotation) o2).getXPathAddress();
							if (xpath1 != null && xpath2 != null) {
								return xpath1.compareTo(xpath2);
							}
						}

						return o1.address().compareTo(o2.address());
					}
				});
	}

	private final LmnlEventHandler DEBUG_HANDLER = new LmnlEventHandler() {
		private int depth = 0;

		public void startDocument(LmnlDocument document) {
			printDebugMessage(indent() + String.format("DocumentStart[%s{%s}]", document.getQName(),//
					(document.getId() == null ? "" : document.getId())));
			depth++;
		}

		public void startAnnotation(LmnlAnnotation annotation) {
			LmnlRange address = annotation.address();
			printDebugMessage(indent() + String.format("AnnotationStart[%s{%s}]: [%d,%d]", annotation.getQName(),//
					(annotation.getId() == null ? "" : annotation.getId()), address.start, address.end));
			depth++;
		}

		public void endDocument(LmnlDocument document) {
			depth--;
			printDebugMessage(indent() + String.format("DocumentEnd[%s{%s}]", document.getQName(),//
					(document.getId() == null ? "" : document.getId())));
		}

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
