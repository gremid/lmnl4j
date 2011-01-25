package org.lmnl.xml;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;

import org.lmnl.LmnlAnnotation;
import org.lmnl.LmnlAnnotationFactory;
import org.lmnl.LmnlDocument;
import org.lmnl.LmnlLayer;
import org.lmnl.LmnlRange;
import org.lmnl.base.DefaultLmnlAnnotationFactory;
import org.lmnl.base.DefaultLmnlDocument;
import org.lmnl.xml.sax.LmnlBuildingDefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class LmnlXmlUtils {

	public static LmnlDocument buildDocument(XMLReader reader, InputSource source) throws IOException, SAXException {
		LmnlDocument document = createLmnlDocument(source.getSystemId());
		reader.setContentHandler(new LmnlBuildingDefaultHandler(document));
		reader.parse(source);
		return document;
	}

	public static LmnlDocument buildDocument(Transformer transformer, Source source) throws TransformerException {
		LmnlDocument document = createLmnlDocument(source.getSystemId());
		transformer.transform(source, new SAXResult(new LmnlBuildingDefaultHandler(document)));
		return document;
	}

	public static LmnlDocument buildDocument(Source source) throws TransformerException {
		return buildDocument(TransformerFactory.newInstance().newTransformer(), source);
	}

	public static LmnlDocument createLmnlDocument(String systemId) {
		return new DefaultLmnlDocument(systemId == null ? null : URI.create(systemId), null, XML_ANNOTATION_FACTORY);
	}

	private static final LmnlAnnotationFactory XML_ANNOTATION_FACTORY = new DefaultLmnlAnnotationFactory() {

		@SuppressWarnings("unchecked")
		public <T extends LmnlAnnotation> T create(LmnlLayer owner, String prefix, String localName,
				String text, LmnlRange address, java.lang.Class<T> type) {
			if (type.isAssignableFrom(XmlElementAnnotation.class)) {
				return (T) new XmlElementAnnotation(owner, prefix, localName, text, address);
			} else {
				return super.create(owner, prefix, localName, text, address, type);
			}
		};
	};

	public static XmlElementAnnotation copy(XmlElementAnnotation annotation, LmnlLayer to) {
		XmlElementAnnotation copy = to.add(annotation, XmlElementAnnotation.class);
		copy.setAttributes(new HashSet<XmlAttribute>(annotation.getAttributes()));
		copy.setXPathAddress(annotation.getXPathAddress());
		return copy;
	}

	public static void copyProperties(XmlElementAnnotation from, XmlElementAnnotation to) {
		to.setAttributes(new HashSet<XmlAttribute>(from.getAttributes()));
		to.setXPathAddress(from.getXPathAddress());
	}
}
