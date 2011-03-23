package org.lmnl.xml;

import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.lmnl.Annotation;
import org.lmnl.Document;
import org.lmnl.Layer;
import org.lmnl.QName;
import org.lmnl.QNameImpl;
import org.lmnl.TextRepository;

import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;

public abstract class XMLParser {

	private final TransformerFactory transformerFactory;
	private final XMLInputFactory xmlInputFactory;

	private TextRepository textRepository;

	private Charset charset = Charset.forName("UTF-8");
	private boolean removeLeadingWhitespace = true;
	private int textBufferSize = 100000;
	private int xmlEventBatchSize = 1000;

	public XMLParser() {
		transformerFactory = TransformerFactory.newInstance();
		xmlInputFactory = XMLInputFactory.newInstance();
		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);

	}

	public void setTextRepository(TextRepository textRepository) {
		this.textRepository = textRepository;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public void setRemoveLeadingWhitespace(boolean removeLeadingWhitespace) {
		this.removeLeadingWhitespace = removeLeadingWhitespace;
	}

	public void setTextBufferSize(int textBufferSize) {
		this.textBufferSize = textBufferSize;
	}

	public void setXmlEventBatchSize(int xmlEventBatchSize) {
		this.xmlEventBatchSize = xmlEventBatchSize;
	}

	public void load(Document document, Source xml) throws IOException, TransformerException {
		File sourceContents = File.createTempFile(getClass().getName(), ".xml");
		sourceContents.deleteOnExit();

		Reader sourceContentsReader = null;

		try {
			final Transformer serializer = transformerFactory.newTransformer();
			serializer.setOutputProperty(OutputKeys.METHOD, "xml");
			serializer.setOutputProperty(OutputKeys.ENCODING, charset.name());
			serializer.setOutputProperty(OutputKeys.INDENT, "no");
			serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			serializer.transform(xml, new StreamResult(sourceContents));

			sourceContentsReader = new InputStreamReader(new FileInputStream(sourceContents), charset);
			textRepository.setText(document, sourceContentsReader);
		} finally {
			Closeables.close(sourceContentsReader, false);
			sourceContents.delete();
		}
	}

	public void parse(Document document, Layer to, XMLParserConfiguration configuration) throws IOException, XMLStreamException {
		XMLStreamReader xmlReader = null;
		Session session = null;
		try {
			session = new Session(document, to, configuration);
			xmlReader = xmlInputFactory.createXMLStreamReader(textRepository.getText(document));
			int xmlEvents = 0;
			Map<QName, String> attributes = null;

			while (xmlReader.hasNext()) {
				if (xmlEvents++ % xmlEventBatchSize == 0) {
					newXMLEventBatch();
				}

				switch (xmlReader.next()) {
				case XMLStreamConstants.START_ELEMENT:
					session.textNodeBoundary();
					session.nextSibling();

					attributes = Maps.newHashMap();
					final int attributeCount = xmlReader.getAttributeCount();
					for (int ac = 0; ac < attributeCount; ac++) {
						final javax.xml.namespace.QName attrQName = xmlReader.getAttributeName(ac);
						if (XMLNS_ATTRIBUTE_NS_URI.equals(attrQName.getNamespaceURI())) {
							continue;
						}
						attributes.put(new QNameImpl(attrQName), xmlReader.getAttributeValue(ac));
					}

					session.startElement(new QNameImpl(xmlReader.getName()), attributes);
					break;
				case XMLStreamConstants.END_ELEMENT:
					session.endElement();
					session.textNodeBoundary();
					break;
				case XMLStreamConstants.COMMENT:
					session.textNodeBoundary();
					session.nextSibling();

					attributes = Maps.newHashMap();
					attributes.put(QNameImpl.COMMENT_TEXT_QNAME, xmlReader.getText());
					session.startElement(QNameImpl.COMMENT_QNAME, attributes);
					session.endElement();
					break;
				case XMLStreamConstants.PROCESSING_INSTRUCTION:
					session.textNodeBoundary();
					session.nextSibling();

					attributes = Maps.newHashMap();
					attributes.put(QNameImpl.PI_TARGET_QNAME, xmlReader.getPITarget());
					final String data = xmlReader.getPIData();
					if (data != null) {
						attributes.put(QNameImpl.PI_DATA_QNAME, data);
					}

					session.startElement(QNameImpl.PI_QNAME, attributes);
					session.endElement();
					break;
				case XMLStreamConstants.CHARACTERS:
					session.textNode(xmlReader.getText());
					break;
				}
			}

			Reader text = null;
			try {
				textRepository.setText(to, text = session.text());
			} finally {
				Closeables.closeQuietly(text);
			}
		} finally {
			if (xmlReader != null) {
				try {
					xmlReader.close();
				} catch (XMLStreamException e) {
				}
			}
			if (session != null) {
				session.dispose();
			}

		}
	}

	protected abstract Annotation startAnnotation(Document d, Layer in, QName name, Map<QName, String> attrs, int start,
			Iterable<Integer> nodePath);

	protected abstract void endAnnotation(Annotation annotation, int end);

	protected void newXMLEventBatch() {
	}

	private class Session {
		private final Document document;
		private final Layer to;
		private final XMLParserConfiguration config;

		private final Stack<Annotation> elementContext = new Stack<Annotation>();
		private final Stack<Boolean> spacePreservationContext = new Stack<Boolean>();
		private final Stack<Integer> nodePath = new Stack<Integer>();
		private final FileBackedOutputStream textBuffer = new FileBackedOutputStream(textBufferSize);

		private int offset = 0;
		private int textStart = -1;
		private char lastChar = (removeLeadingWhitespace ? ' ' : 0);

		private Session(Document document, Layer to, XMLParserConfiguration config) {
			this.document = document;
			this.to = to;
			this.config = config;
			this.nodePath.push(0);
		}

		private Annotation startElement(QName name, Map<QName, String> attributes) {
			final Annotation annotation = startAnnotation(document, to, name, attributes, offset, nodePath);
		
			elementContext.push(annotation);
			spacePreservationContext.push(spacePreservationContext.isEmpty() ? false : spacePreservationContext.peek());
			for (Map.Entry<QName, String> attr : attributes.entrySet()) {
				if (QNameImpl.XML_SPACE.equals(attr.getKey())) {
					spacePreservationContext.pop();
					spacePreservationContext.push("preserve".equalsIgnoreCase(attr.getValue()));
				}
			}
			nodePath.push(0);
			return annotation;
		}

		private void endElement() throws IOException {
			if (config.getLineElements().contains(elementContext.peek().getName())) {
				spacePreservationContext.push(true);
				textNode("\n");
				spacePreservationContext.pop();
			}

			nodePath.pop();
			spacePreservationContext.pop();
			endAnnotation(elementContext.pop(), offset);
		}

		private void nextSibling() {
			nodePath.push(nodePath.pop() + 1);
		}

		private void textNode(String text) throws IOException {
			boolean preserveSpace = !spacePreservationContext.isEmpty() && spacePreservationContext.peek();
			if (!preserveSpace && !elementContext.isEmpty() && config.getContainerElements().contains(elementContext.peek().getName())) {
				return;
			}

			if (textStart < 0) {
				nextSibling();
				textStart = offset;
			}

			for (int cc = 0; cc < text.length(); cc++) {
				final char currentChar = text.charAt(cc);
				if (!preserveSpace && Character.isWhitespace(lastChar) && Character.isWhitespace(currentChar)) {
					continue;
				}
				textBuffer.write(Character.toString(lastChar = currentChar).getBytes(charset));
				offset++;
			}
		}

		private void textNodeBoundary() {
			if (textStart >= 0 && offset > textStart) {
				Annotation text = startAnnotation(document, to, QNameImpl.TEXT_QNAME, Maps.<QName, String> newHashMap(),
						textStart, nodePath);
				endAnnotation(text, offset);
				textStart = -1;
			}
		}

		private Reader text() throws IOException {
			return new InputStreamReader(textBuffer.getSupplier().getInput(), charset);
		}

		private void dispose() throws IOException {
			textBuffer.reset();
		}
	}
}
