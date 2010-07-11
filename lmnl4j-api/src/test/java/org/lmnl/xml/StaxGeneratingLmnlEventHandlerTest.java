package org.lmnl.xml;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.lmnl.AbstractXmlTest;
import org.lmnl.event.LmnlEventGenerator;
import org.w3c.dom.Document;

public class StaxGeneratingLmnlEventHandlerTest extends AbstractXmlTest {

	@Test
	public void serialize() throws Exception {
		DocumentBuilder domBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dom = domBuilder.newDocument();

		XMLStreamWriter writer = null;
		try {
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(new DOMResult(dom));
			new LmnlEventGenerator().generate(document(), new StaxGeneratingLmnlEventHandler(writer));
		} finally {
			if (writer != null) {
				writer.close();
			}
		}

		if (LOG.isDebugEnabled()) {
			StringWriter out = new StringWriter();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(2));
			transformer.transform(new DOMSource(dom), new StreamResult(out));
			printDebugMessage(out.toString());
		}

	}
}
