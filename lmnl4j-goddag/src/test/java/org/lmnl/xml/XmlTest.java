package org.lmnl.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Before;
import org.lmnl.GraphDbBasedTest;
import org.neo4j.graphdb.RelationshipType;

public abstract class XmlTest extends GraphDbBasedTest {
	private static final String[] XML_RESOURCES = new String[] { "/archimedes-palimpsest-tei.xml", "/george-algabal-tei.xml" };

	protected XMLInputFactory inputFactory;
	protected XMLOutputFactory outputFactory;
	protected XmlAnnotationNodeFactory nodeFactory;
	protected List<Document> documents;

	@Before
	public void readTestResources() throws XMLStreamException, IOException {
		inputFactory = XMLInputFactory.newInstance();
		
		outputFactory = XMLOutputFactory.newInstance();
		outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
		
		nodeFactory = new XmlAnnotationNodeFactory(db);
		
		documents = new LinkedList<Document>();
		for (String xmlResource : XML_RESOURCES) {
			InputStream xmlStream = null;
			XMLStreamReader xmlStreamReader = null;
			try {
				Document document = nodeFactory.createNode(Document.class);
				db.getReferenceNode().createRelationshipTo(document.getUnderlyingNode(), TEST_REL);

				xmlStream = getClass().getResourceAsStream(xmlResource);

				xmlStreamReader = inputFactory.createXMLStreamReader(xmlStream);
				document.importFromStream(xmlStreamReader);
				documents.add(document);
			} finally {
				if (xmlStreamReader != null) {
					xmlStreamReader.close();
				}
				if (xmlStream != null) {
					xmlStream.close();
				}
			}
		}
	}

	protected void print(XmlAnnotationNode node) throws Exception {
		XMLStreamWriter writer = outputFactory.createXMLStreamWriter(System.out);
		writer.setPrefix("tei", "http://www.tei-c.org/ns/1.0");
		node.exportToStream(writer);
		
		System.out.println();

	}

	protected static final RelationshipType TEST_REL = new RelationshipType() {

		@Override
		public String name() {
			return "TEST";
		}
	};
}
