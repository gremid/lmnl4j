package org.lmnl.xml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.xml.XMLConstants;

import org.lmnl.Document;
import org.lmnl.Range;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLImportHandler extends DefaultHandler {
	private Document document;

	private StringBuilder contentBuf;
	private Stack<StartedAnnotation> openElements;
	private Stack<Integer> nodePosition;

	public XMLImportHandler(Document document) {
		this.document = document;
	}

	@Override
	public void startDocument() throws SAXException {
		contentBuf = new StringBuilder();
		openElements = new Stack<StartedAnnotation>();
		nodePosition = new Stack<Integer>();
		document.setText(null);
	}

	@Override
	public void endDocument() throws SAXException {
		document.setText(contentBuf.toString());
		contentBuf = null;
		openElements = null;
		nodePosition = null;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
		if (uri == null) {
			throw new SAXException("Parser is not namespace aware");
		}

		if (!nodePosition.isEmpty()) {
			nodePosition.push(nodePosition.pop() + 1);
		}
		int[] address = new int[nodePosition.size()];
		for (int ai = 0; ai < address.length; ai++) {
			address[ai] = nodePosition.get(ai);
		}

		final URI ns = URI.create(uri);
		String prefix = document.getPrefix(ns);
		if (prefix == null) {
			prefix = (qName.contains(":") ? qName.substring(0, qName.indexOf(":")) : "");
			document.addNamespace(prefix, ns);
		}
		
		final Set<XMLAttribute> attributes = new HashSet<XMLAttribute>();
		for (int ac = 0; ac < attrs.getLength(); ac++) {
			String attrQName = attrs.getQName(ac);
			String attrUri = attrs.getURI(ac);
			if (attrUri == null || attrUri.length() == 0) {
				attrUri = uri;
			}
			if (attrQName.startsWith(XMLConstants.XMLNS_ATTRIBUTE) || XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attrUri)) {
				continue;
			}

			final URI attrNs = URI.create(attrUri);
			String attrPrefix = document.getPrefix(attrNs);
			if (attrPrefix == null) {
				attrPrefix = attrQName.contains(":") ? attrQName.substring(0, attrQName.indexOf(":")) : "";
				document.addNamespace(attrPrefix, attrNs);
			}
			
			final String attrLocalName = attrs.getLocalName(ac);
			final String attrValue = attrs.getValue(ac);

			attributes.add(new XMLAttribute(attrPrefix, attrLocalName, attrValue));
		}

		openElements.push(new StartedAnnotation(prefix, localName, contentBuf.length(), attributes, new XPath(address)));
		nodePosition.push(0);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		final StartedAnnotation sa = openElements.pop();
		final XMLElement annotation = document.add(sa.prefix, sa.localName, null, new Range(sa.start, contentBuf.length()), XMLElement.class);
		annotation.setXPathAddress(sa.xpath);
		annotation.setAttributes(sa.attributes);
		
		for (XMLAttribute attr : sa.attributes) {
			if (document.getNamespace(attr.prefix).equals(XMLConstants.XML_NS_URI) && "id".equals(attr.localName)) {
				try {
					annotation.setId(new URI(null, null, attr.value));
				} catch (URISyntaxException e) {
					throw new IllegalArgumentException("Invalid xml:id value (no URI fragment)", e);
				}
				continue;
			}
		}

		nodePosition.pop();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		contentBuf.append(ch, start, length);
	}
	
	private static class StartedAnnotation {
		private final String prefix;
		private final String localName;
		private final int start;
		private final Set<XMLAttribute> attributes;
		private final XPath xpath;

		private StartedAnnotation(String prefix, String localName, int start, Set<XMLAttribute> attributes, XPath xpath) {
			this.prefix = prefix;
			this.localName = localName;
			this.start = start;
			this.attributes = attributes;
			this.xpath = xpath;
		}
	}
}