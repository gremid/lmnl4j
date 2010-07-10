package org.lmnl.xml;

import java.io.IOException;
import java.net.URI;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;

import org.lmnl.LmnlDocument;
import org.lmnl.base.DefaultLmnlDocument;
import org.lmnl.xml.sax.LmnlBuildingDefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class LmnlXmlUtils {
	public static void build(XMLReader reader, InputSource source, XmlElementAnnotationFactory factory) throws IOException, SAXException {
		reader.setContentHandler(new LmnlBuildingDefaultHandler(factory));
		reader.parse(source);
	}

	public static LmnlDocument buildDocument(XMLReader reader, InputSource source) throws IOException, SAXException {
		final String systemId = source.getSystemId();
		DefaultLmnlDocument document = new DefaultLmnlDocument(systemId == null ? null : URI.create(systemId));
		build(reader, source, new DefaultXmlElementAnnotationFactory(document));
		return document;
	}

	public static void build(Transformer transformer, Source source, XmlElementAnnotationFactory factory) throws TransformerException {
		transformer.transform(source, new SAXResult(new LmnlBuildingDefaultHandler(factory)));
	}

	public static void build(Source source, XmlElementAnnotationFactory factory) throws TransformerException {
		build(TransformerFactory.newInstance().newTransformer(), source, factory);
	}

	public static LmnlDocument buildDocument(Transformer transformer, Source source) throws TransformerException {
		final String systemId = source.getSystemId();
		DefaultLmnlDocument document = new DefaultLmnlDocument(systemId == null ? null : URI.create(systemId));
		build(transformer, source, new DefaultXmlElementAnnotationFactory(document));
		return document;
	}

	public static LmnlDocument buildDocument(Source source) throws TransformerException {
		final String systemId = source.getSystemId();
		DefaultLmnlDocument document = new DefaultLmnlDocument(systemId == null ? null : URI.create(systemId));
		build(TransformerFactory.newInstance().newTransformer(), source, new DefaultXmlElementAnnotationFactory(document));
		return document;
	}
}
