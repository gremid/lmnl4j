package org.lmnl.event;

import org.junit.Test;
import org.lmnl.AbstractXMLTest;
import org.lmnl.Annotation;
import org.lmnl.Document;
import org.lmnl.Range;
import org.lmnl.xml.XPath;
import org.lmnl.xml.XMLElement;

import com.google.common.collect.Ordering;

public class EventGeneratorTest extends AbstractXMLTest {

	@Test
	public void serializeWithRangeOrder() throws EventHandlerException {
		new EventGenerator().generate(document("george-algabal-tei.xml"), DEBUG_HANDLER);
	}

	@Test
	public void serializeWithXmlNodeAddressOrder() throws EventHandlerException {
		new EventGenerator().generate(document("george-algabal-tei.xml"), DEBUG_HANDLER,
				new Ordering<Annotation>() {

					public int compare(Annotation o1, Annotation o2) {
						if (o1 instanceof XMLElement && o2 instanceof XMLElement) {
							XPath xpath1 = ((XMLElement) o1).getXPathAddress();
							XPath xpath2 = ((XMLElement) o2).getXPathAddress();
							if (xpath1 != null && xpath2 != null) {
								return xpath1.compareTo(xpath2);
							}
						}

						return o1.address().compareTo(o2.address());
					}
				});
	}

	private final EventHandler DEBUG_HANDLER = new EventHandler() {
		private int depth = 0;

		public void startDocument(Document document) {
			printDebugMessage(indent() + String.format("DocumentStart[%s{%s}]", document.getQName(),//
					(document.getId() == null ? "" : document.getId())));
			depth++;
		}

		public void startAnnotation(Annotation annotation) {
			Range address = annotation.address();
			printDebugMessage(indent() + String.format("AnnotationStart[%s{%s}]: [%d,%d]", annotation.getQName(),//
					(annotation.getId() == null ? "" : annotation.getId()), address.start, address.end));
			depth++;
		}

		public void endDocument(Document document) {
			depth--;
			printDebugMessage(indent() + String.format("DocumentEnd[%s{%s}]", document.getQName(),//
					(document.getId() == null ? "" : document.getId())));
		}

		public void endAnnotation(Annotation annotation) {
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
