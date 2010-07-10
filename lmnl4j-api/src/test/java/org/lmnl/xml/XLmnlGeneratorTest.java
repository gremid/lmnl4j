package org.lmnl.xml;

import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;
import org.lmnl.AbstractXmlSourcedTest;

public class XLmnlGeneratorTest extends AbstractXmlSourcedTest {

	@Test
	public void serialize() throws Exception {
		StringWriter out = new StringWriter();
		XMLStreamWriter writer = null;
		try {
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
			writer.writeStartDocument();
			new XLmnlGenerator(writer).generate(document());
			writer.writeEndDocument();
			writer.flush();
			printDebugMessage(out.toString());
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
}
