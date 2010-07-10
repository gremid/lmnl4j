package org.lmnl.xml;

import java.io.IOException;
import java.net.URI;

import org.lmnl.lom.LmnlDocument;
import org.lmnl.lom.base.DefaultLmnlDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class LmnlXmlUtils {

	private static final URI DEFAULT_DOCUMENT_BASE = null;

	public static LmnlDocument buildDocument(XMLReader reader, InputSource source) throws IOException, SAXException {
		URI base = (source.getSystemId() == null ? DEFAULT_DOCUMENT_BASE : URI.create(source.getSystemId()));
		DefaultLmnlDocument document = new DefaultLmnlDocument(base);
		build(reader, source, new GenericXmlBasedLmnlAnnotationFactory(document));
		return document;
	}

	public static void build(XMLReader reader, InputSource source, XmlBasedLmnlAnnotationFactory factory) throws IOException, SAXException {
		reader.setContentHandler(new LmnlBuildingDefaultHandler(factory));
		reader.parse(source);
	}
}
