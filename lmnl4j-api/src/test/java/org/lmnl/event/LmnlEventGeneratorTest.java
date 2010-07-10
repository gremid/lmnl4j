package org.lmnl.event;

import org.junit.Test;
import org.lmnl.AbstractXmlSourcedTest;
import org.lmnl.lom.LmnlAnnotation;
import org.lmnl.lom.LmnlDocument;
import org.lmnl.lom.LmnlRangeAddress;
import org.lmnl.xml.XmlNodeSourced;

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
				if (o1 instanceof XmlNodeSourced && o2 instanceof XmlNodeSourced) {
					return XmlNodeSourced.COMPARATOR.compare((XmlNodeSourced) o1, (XmlNodeSourced) o2);
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
			LmnlRangeAddress address = annotation.address();
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
