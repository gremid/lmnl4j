package org.lmnl.xml.sax;

import java.net.URI;
import java.util.Stack;

import org.lmnl.lom.LmnlAnnotation;
import org.lmnl.xml.XPathAddress;
import org.lmnl.xml.XmlElementAnnotationFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LmnlBuildingDefaultHandler extends DefaultHandler {
	private XmlElementAnnotationFactory af;

	private StringBuilder contentBuf;
	private Stack<LmnlAnnotation> openElements;
	private Stack<Integer> nodePosition;

	public LmnlBuildingDefaultHandler(XmlElementAnnotationFactory factory) {
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

		if (!nodePosition.isEmpty()) {
			nodePosition.push(nodePosition.pop() + 1);
		}
		int[] address = new int[nodePosition.size()];
		for (int ai = 0; ai < address.length; ai++) {
			address[ai] = nodePosition.get(ai);
		}

		String prefix = (qName.contains(":") ? qName.substring(0, qName.indexOf(":")) : "");
		openElements.push(af.startElement(URI.create(uri), prefix, localName, attrs, new XPathAddress(address), contentBuf.length()));
		nodePosition.push(0);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		af.endElement(openElements.pop(),  contentBuf.length());
		nodePosition.pop();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		contentBuf.append(ch, start, length);
	}
}
