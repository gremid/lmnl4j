package org.lmnl.event;

import org.junit.Test;
import org.lmnl.AbstractXMLTest;
import org.lmnl.Annotation;
import org.springframework.beans.factory.annotation.Autowired;

public class EventGeneratorTest extends AbstractXMLTest {

	@Autowired
	private EventGenerator generator;
	
	@Test
	public void generateEvents() throws EventHandlerException {
		generator.generate(document("george-algabal-tei.xml"), DEBUG_HANDLER);
	}

	private final EventHandler DEBUG_HANDLER = new EventHandler() {

		public void startAnnotation(Annotation annotation, int depth) {
			printDebugMessage(indent(depth) + "START: " + annotation);
		}

		public void endAnnotation(Annotation annotation, int depth) {
			printDebugMessage(indent(depth) + "END: " + annotation);
		}

		private String indent(int depth) {
			StringBuilder indent = new StringBuilder(depth);
			for (int dc = 0; dc < depth; dc++) {
				indent.append("\t");
			}
			return indent.toString();
		}
	};
}
