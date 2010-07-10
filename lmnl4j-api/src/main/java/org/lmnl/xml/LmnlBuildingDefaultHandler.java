package org.lmnl.xml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Stack;

import javax.xml.XMLConstants;

import org.lmnl.lom.LmnlAnnotation;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LmnlBuildingDefaultHandler extends DefaultHandler {
	private XmlBasedLmnlAnnotationFactory af;

	private StringBuilder contentBuf;
	private Stack<LmnlAnnotation> openElements;
	private Stack<Integer> nodePosition;

	public LmnlBuildingDefaultHandler(XmlBasedLmnlAnnotationFactory factory) {
		this.af = factory;
	}

	@Override
	public void startDocument() throws SAXException {
		contentBuf = new StringBuilder();
		openElements = new Stack<LmnlAnnotation>();
		nodePosition = new Stack<Integer>();
	}

	@Override
	public void endDocument() throws SAXException {
		af.createText(contentBuf.toString());
		contentBuf = null;
		openElements = null;
		nodePosition = null;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
		if (uri == null) {
			throw new SAXException("Parser is not namespace aware");
		}
		String prefix = (qName.contains(":") ? qName.substring(0, qName.indexOf(":")) : "");
		LmnlAnnotation lmnlElement = af.createElementRange(URI.create(uri), prefix, localName, contentBuf.length());
		openElements.push(lmnlElement);

		if (!nodePosition.isEmpty()) {
			nodePosition.push(nodePosition.pop() + 1);
		}

		if (lmnlElement instanceof XmlNodeSourced) {
			setXmlNodeAddress((XmlNodeSourced) lmnlElement);
		}

		for (int ac = 0; ac < attrs.getLength(); ac++) {
			String attrQName = attrs.getQName(ac);
			String attrUri = attrs.getURI(ac);
			if (attrQName.startsWith(XMLConstants.XMLNS_ATTRIBUTE) || XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attrUri)) {
				continue;
			}

			String attrPrefix = (attrQName.contains(":") ? attrQName.substring(0, attrQName.indexOf(":")) : "");
			String attrLocalName = attrs.getLocalName(ac);
			String attrValue = attrs.getValue(ac);
			if (attrUri == null || attrUri.length() == 0) {
				attrUri = uri;
				attrPrefix = prefix;
			}

			if (attrUri.equals(XMLConstants.XML_NS_URI) && "id".equals(attrLocalName)) {
				try {
					lmnlElement.setId(new URI(null, null, attrValue));
				} catch (URISyntaxException e) {
					throw new SAXException("Invalid xml:id value (no URI fragment)", e);
				}
				continue;
			}

			LmnlAnnotation attr = af.createAttributeAnnotation(lmnlElement, URI.create(attrUri), attrPrefix, attrLocalName, attrValue);
			if (attr instanceof XmlNodeSourced) {
				nodePosition.push(0);
				setXmlNodeAddress((XmlNodeSourced) attr);
				nodePosition.pop();
			}
		}

		nodePosition.push(0);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		openElements.pop().address().end = contentBuf.length();
		nodePosition.pop();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		contentBuf.append(ch, start, length);
	}

	private void setXmlNodeAddress(XmlNodeSourced annotation) {
		int[] address = new int[nodePosition.size()];
		for (int ai = 0; ai < address.length; ai++) {
			address[ai] = nodePosition.get(ai);
		}
		annotation.setXmlNodeAddress(new XmlNodeAddress(address));
	}
}
