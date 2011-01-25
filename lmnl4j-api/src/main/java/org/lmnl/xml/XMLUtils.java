package org.lmnl.xml;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;

import org.lmnl.Annotation;
import org.lmnl.AnnotationFactory;
import org.lmnl.Document;
import org.lmnl.Layer;
import org.lmnl.Range;
import org.lmnl.base.DefaultAnnotationFactory;
import org.lmnl.base.DefaultDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class XMLUtils {

	public static Document buildDocument(XMLReader reader, InputSource source) throws IOException, SAXException {
		Document document = createLmnlDocument(source.getSystemId());
		reader.setContentHandler(new XMLImportHandler(document));
		reader.parse(source);
		return document;
	}

	public static Document buildDocument(Transformer transformer, Source source) throws TransformerException {
		Document document = createLmnlDocument(source.getSystemId());
		transformer.transform(source, new SAXResult(new XMLImportHandler(document)));
		return document;
	}

	public static Document buildDocument(Source source) throws TransformerException {
		return buildDocument(TransformerFactory.newInstance().newTransformer(), source);
	}

	public static Document createLmnlDocument(String systemId) {
		return new DefaultDocument(systemId == null ? null : URI.create(systemId), null, XML_ANNOTATION_FACTORY);
	}

	private static final AnnotationFactory XML_ANNOTATION_FACTORY = new DefaultAnnotationFactory() {

		@SuppressWarnings("unchecked")
		public <T extends Annotation> T create(Layer owner, String prefix, String localName,
				String text, Range address, java.lang.Class<T> type) {
			if (type.isAssignableFrom(XMLElement.class)) {
				return (T) new XMLElement(owner, prefix, localName, text, address);
			} else {
				return super.create(owner, prefix, localName, text, address, type);
			}
		};
	};

	public static XMLElement copy(XMLElement annotation, Layer to) {
		XMLElement copy = to.add(annotation, XMLElement.class);
		copy.setAttributes(new HashSet<XMLAttribute>(annotation.getAttributes()));
		copy.setXPathAddress(annotation.getXPathAddress());
		return copy;
	}

	public static void copyProperties(XMLElement from, XMLElement to) {
		to.setAttributes(new HashSet<XMLAttribute>(from.getAttributes()));
		to.setXPathAddress(from.getXPathAddress());
	}
}
