/**
 * Layered Markup and Annotation Language for Java (lmnl4j):
 * implementation of LMNL, a markup language supporting layered and/or
 * overlapping annotations.
 *
 * Copyright (C) 2010 the respective authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lmnl.xml;

import java.io.IOException;
import java.net.URI;
import java.util.Stack;

import javax.xml.XMLConstants;

import org.codehaus.jackson.JsonGenerator;
import org.lmnl.lom.LmnlAnnotation;
import org.lmnl.lom.LmnlLayer;
import org.lmnl.lom.LmnlRange;
import org.lmnl.lom.base.DefaultLmnlAnnotation;
import org.lmnl.lom.base.DefaultLmnlRange;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A SAX-based converter, transforming XML documents into LOM models.
 * 
 * <p/>
 * 
 * Can be used in conjunction with {@link PlainTextXmlFilter} to read in and
 * tidy up arbitrary, text-centric XML documents.
 * 
 * <p/>
 * 
 * The converter does not strive to convert the complete XML infoset, so for
 * example comments and processing instructions are ignored. What can be
 * remembered though is each node's position within the XML DOM.
 * 
 * <p/>
 * 
 * Also note, that the converter currently works with namespaced documents and
 * namespace-aware parsers only.
 * 
 * @see XmlNodeAddress
 * @see XmlNodeSourced
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class SaxBasedLmnlBuilder {
	/**
	 * Interface to factories used by this converter to create corresponding
	 * annotations for XML elements and attributes.
	 * 
	 * <p/>
	 * 
	 * Depending on whether a factory creates annotations, that implement
	 * {@link XmlNodeSourced}, the converter will optionally assign
	 * information of relative node positions to the created annotations.
	 * 
	 * @author <a href="http://gregor.middell.net/"
	 *         title="Homepage of Gregor Middell">Gregor Middell</a>
	 * 
	 */
	public interface AnnotationFactory {
		/**
		 * Creates a range annotation corresponding to an XML element.
		 * 
		 * @param ns
		 *                the namespace URI of the XML element
		 * @param prefix
		 *                the prefix of the element's name
		 * @param localName
		 *                the element's local name
		 * @param startOffset
		 *                the offset of the range's start. Note that the
		 *                end offset will be set later on the range's
		 *                address, accessing it via
		 *                {@link LmnlRange#address()}
		 * @return a range annotation representing the XML element
		 */
		LmnlRange createElementRange(URI ns, String prefix, String localName, int startOffset);

		/**
		 * Creates an annotation corresponding to an XML attribute.
		 * 
		 * @param ns
		 *                the namespace URI of the XML attribute,
		 *                defaulting to the attribute's element
		 *                namespace
		 * @param prefix
		 *                the attribute's namespace prefix
		 * @param localName
		 *                the local name of the attribute
		 * @param value
		 *                the attribute's value
		 * @return an annotation representing the XML attribute
		 */
		LmnlAnnotation createAttributeAnnotation(LmnlRange elementRange, URI ns, String prefix, String localName, String value);
	}

	/**
	 * Creates a converter using a default XML reader for parsing incoming
	 * documents.
	 * 
	 * @see #SaxBasedLmnlBuilder(XMLReader)
	 */
	public SaxBasedLmnlBuilder() {
		try {
			setReader(XMLReaderFactory.createXMLReader());
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a converter using the given XML reader for parsing incoming
	 * documents.
	 * 
	 * @param reader
	 *                a XML reader used to parse documents
	 * 
	 * @see PlainTextXmlFilter
	 */
	public SaxBasedLmnlBuilder(XMLReader reader) {
		setReader(reader);
	}

	/**
	 * Configures the XML reader used to parse incoming documents after the
	 * converter's construction.
	 * 
	 * @param reader
	 *                a XML reader used to parse documents
	 */
	public void setReader(XMLReader reader) {
		this.reader = reader;
	}

	/**
	 * Configures the factory responsible for constructing annotations
	 * corresponding to converte XML nodes.
	 * 
	 * <p/>
	 * 
	 * If not called, a default implementation is used, that creates
	 * in-memory annotations, which also implement {@link XmlNodeSourced}
	 * and thus represent the relative positions of XML elements/attributes.
	 * 
	 * @param annotationFactory
	 *                an object implementing the factory interface
	 */
	public void setAnnotationFactory(AnnotationFactory annotationFactory) {
		this.annotationFactory = annotationFactory;
	}

	/**
	 * Builds a LOM structure by converting the given XML input source.
	 * 
	 * @param source
	 *                the XML input source to convert
	 * @param destination
	 *                the layer, in which the corresponding LOM structure is
	 *                built
	 * @throws SAXException
	 *                 if an XML-related parser error occurs
	 * @throws IOException
	 *                 if an I/O related error occurs while parsing the XML
	 *                 input
	 */
	public void build(final InputSource source, final LmnlLayer destination) throws SAXException, IOException {
		reader.setContentHandler(new DefaultHandler() {
			private StringBuilder contentBuf = new StringBuilder();
			private Stack<LmnlRange> openElements = new Stack<LmnlRange>();
			private Stack<Integer> nodePosition = new Stack<Integer>();
			private URI sourceDocument = null;

			@Override
			public void startDocument() throws SAXException {
				if (source.getSystemId() != null) {
					try {
						sourceDocument = URI.create(source.getSystemId());
					} catch (IllegalArgumentException e) {
					}
				}
			}

			@Override
			public void endDocument() throws SAXException {
				destination.setText(contentBuf.toString());
			}

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
				if (uri == null) {
					throw new SAXException("Parser is not namespace aware");
				}
				String prefix = (qName.contains(":") ? qName.substring(0, qName.indexOf(":")) : "");
				LmnlRange lmnlElement = annotationFactory.createElementRange(URI.create(uri), prefix, localName, contentBuf.length());
				destination.add(lmnlElement);
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
						lmnlElement.setId(attrValue);
						continue;
					}

					LmnlAnnotation attr = annotationFactory.createAttributeAnnotation(lmnlElement, URI.create(attrUri), attrPrefix, attrLocalName, attrValue);
					lmnlElement.add(attr);
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
				annotation.setXmlNodeAddress(new XmlNodeAddress(sourceDocument, address));
			}
		});
		reader.parse(source);
	}

	private XMLReader reader;
	private AnnotationFactory annotationFactory = new AnnotationFactory() {

		@Override
		public LmnlRange createElementRange(URI ns, String prefix, String localName, int startOffset) {
			return new XmlElement(ns, prefix, localName, startOffset);
		}

		@Override
		public LmnlAnnotation createAttributeAnnotation(LmnlRange elementRange, URI ns, String prefix, String localName, String value) {
			return new XmlAttribute(ns, prefix, localName, value);
		}
	};

	private static class XmlElement extends DefaultLmnlRange implements XmlNodeSourced {
		protected XmlNodeAddress xmlNodeAddress;

		public XmlElement(URI uri, String prefix, String localName, int start) {
			super(uri, prefix, localName, null, start, start);
		}

		@Override
		public XmlNodeAddress getXmlNodeAddress() {
			return xmlNodeAddress;
		}

		public void setXmlNodeAddress(XmlNodeAddress xmlNodeAddress) {
			this.xmlNodeAddress = xmlNodeAddress;
		}

		@Override
		public int compareTo(XmlNodeSourced o) {
			return XmlNodeSourced.COMPARATOR.compare(this, o);
		}

		@Override
		protected void serializeAttributes(JsonGenerator jg) throws IOException {
			super.serializeAttributes(jg);
			jg.writeFieldName("xmlNode");
			xmlNodeAddress.serialize(jg);
		}

	}

	private static class XmlAttribute extends DefaultLmnlAnnotation implements XmlNodeSourced {

		protected XmlNodeAddress xmlNodeAddress;

		public XmlAttribute(URI uri, String prefix, String localName, String text) {
			super(uri, prefix, localName, text);
		}

		public XmlNodeAddress getXmlNodeAddress() {
			return xmlNodeAddress;
		}

		public void setXmlNodeAddress(XmlNodeAddress xmlNodeAddress) {
			this.xmlNodeAddress = xmlNodeAddress;
		}

		@Override
		public int compareTo(XmlNodeSourced o) {
			return XmlNodeSourced.COMPARATOR.compare(this, o);
		}

		@Override
		protected void serializeAttributes(JsonGenerator jg) throws IOException {
			jg.writeFieldName("xmlNode");
			xmlNodeAddress.serialize(jg);
		}
	}

}
