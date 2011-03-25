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

import org.lmnl.Layer;
import org.lmnl.QName;
import org.lmnl.QNameImpl;
import org.lmnl.Range;
import org.lmnl.TextRepository;

import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;

public abstract class XMLParser {
	public static final QName OFFSET_DELTA_NAME = new QNameImpl(Layer.LMNL_NS_URI, "offset");
	public static final QName NODE_PATH_NAME = new QNameImpl(Layer.LMNL_NS_URI, "xmlNode");

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
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
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

	public void load(Layer layer, Source xml) throws IOException, TransformerException {
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
			updateText(layer, sourceContentsReader);
		} finally {
			Closeables.close(sourceContentsReader, false);
			sourceContents.delete();
		}
	}

	public void parse(Layer source, Layer target, Layer offsetDeltas, XMLParserConfiguration configuration) throws IOException,
			XMLStreamException {
		XMLStreamReader reader = null;
		Session session = null;
		try {
			reader = xmlInputFactory.createXMLStreamReader(textRepository.getText(source));
			session = new Session(target, offsetDeltas, configuration);
			int xmlEvents = 0;
			Map<QName, String> attributes = null;

			while (reader.hasNext()) {
				if (xmlEvents++ % xmlEventBatchSize == 0) {
					newXMLEventBatch();
				}

				switch (reader.next()) {
				case XMLStreamConstants.START_ELEMENT:
					session.writeText();
					session.nextSibling();

					attributes = Maps.newHashMap();
					final int attributeCount = reader.getAttributeCount();
					for (int ac = 0; ac < attributeCount; ac++) {
						final javax.xml.namespace.QName attrQName = reader.getAttributeName(ac);
						if (XMLNS_ATTRIBUTE_NS_URI.equals(attrQName.getNamespaceURI())) {
							continue;
						}
						attributes.put(new QNameImpl(attrQName), reader.getAttributeValue(ac));
					}

					session.startElement(new QNameImpl(reader.getName()), attributes);
					break;
				case XMLStreamConstants.END_ELEMENT:
					session.writeText();
					session.endElement();
					break;
				case XMLStreamConstants.COMMENT:
					session.writeText();
					session.nextSibling();

					attributes = Maps.newHashMap();
					attributes.put(QNameImpl.COMMENT_TEXT_QNAME, reader.getText());
					session.startElement(QNameImpl.COMMENT_QNAME, attributes);
					session.endElement();
					break;
				case XMLStreamConstants.PROCESSING_INSTRUCTION:
					session.writeText();
					session.nextSibling();

					attributes = Maps.newHashMap();
					attributes.put(QNameImpl.PI_TARGET_QNAME, reader.getPITarget());
					final String data = reader.getPIData();
					if (data != null) {
						attributes.put(QNameImpl.PI_DATA_QNAME, data);
					}

					session.startElement(QNameImpl.PI_QNAME, attributes);
					session.endElement();
					break;
				case XMLStreamConstants.CHARACTERS:
				case XMLStreamConstants.ENTITY_REFERENCE:
				case XMLStreamConstants.CDATA:
					session.text(reader.getText(), reader.getLocation().getCharacterOffset());
					break;
				case XMLStreamConstants.END_DOCUMENT:
					session.end();
					break;
				}
			}

			Reader text = null;
			try {
				updateText(target, text = session.read());
			} finally {
				Closeables.closeQuietly(text);
			}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (XMLStreamException e) {
				}
			}
			if (session != null) {
				session.dispose();
			}

		}
	}

	protected abstract void updateText(Layer layer, Reader reader) throws IOException;

	protected abstract Layer startAnnotation(Layer in, QName name, Map<QName, String> attrs, int start,
			Iterable<Integer> nodePath);

	protected abstract void endAnnotation(Layer annotation, int end);

	protected abstract void newOffsetDeltaRange(Layer offsetDeltas, Range range, int offsetDelta);

	protected void newXMLEventBatch() {
	}

	private class Session {
		private final Layer target;
		private final Layer offsetDeltas;
		private final XMLParserConfiguration configuration;

		private final Stack<Layer> elementContext = new Stack<Layer>();
		private final Stack<Boolean> spacePreservationContext = new Stack<Boolean>();
		private final Stack<Boolean> inclusionContext = new Stack<Boolean>();
		private final Stack<Integer> nodePath = new Stack<Integer>();
		private final FileBackedOutputStream textBuffer = new FileBackedOutputStream(textBufferSize);

		private int offset = 0;
		private int startOffset = -1;
		private int offsetDelta = 0;
		private int lastOffsetDeltaChange = 0;

		private char notableCharacter;
		private char lastChar = (removeLeadingWhitespace ? ' ' : 0);

		private Session(Layer target, Layer offsetDeltas, XMLParserConfiguration configuration) {
			this.target = target;
			this.offsetDeltas = offsetDeltas;
			this.configuration = configuration;
			this.notableCharacter = configuration.getNotableCharacter();
			this.nodePath.push(0);
		}

		private Layer startElement(QName name, Map<QName, String> attributes) throws IOException {
			final boolean notable = configuration.isNotable(name);
			final boolean lineElement = configuration.isLineElement(name);
			if (notable || lineElement) {
				writeOffsetDelta();

				if (lineElement && offset > 0) {
					textBuffer.write(Character.toString(lastChar = '\n').getBytes(charset));
					offset++;
				}
				if (notable) {
					textBuffer.write(Character.toString(lastChar = notableCharacter).getBytes(charset));
					offset++;
				}

				lastOffsetDeltaChange = offset;
			}

			final Layer annotation = startAnnotation(target, name, attributes, offset, nodePath);

			elementContext.push(annotation);

			final boolean parentIncluded = (inclusionContext.isEmpty() ? true : inclusionContext.peek());
			inclusionContext.push(parentIncluded ? !configuration.excluded(name) : configuration.included(name));

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
			nodePath.pop();
			spacePreservationContext.pop();
			inclusionContext.pop();
			endAnnotation(elementContext.pop(), offset);
		}

		private void nextSibling() {
			nodePath.push(nodePath.pop() + 1);
		}

		private void text(String text, int sourceOffset) throws IOException {
			if (startOffset < 0) {
				nextSibling();
				startOffset = offset;
			}

			if (!inclusionContext.isEmpty() && !inclusionContext.peek()) {
				return;
			}

			final boolean preserveSpace = !spacePreservationContext.isEmpty() && spacePreservationContext.peek();
			if (!preserveSpace && !elementContext.isEmpty()
					&& configuration.isContainerElement(elementContext.peek().getName())) {
				return;
			}

			final int newOffsetDelta = sourceOffset - offset;
			if (newOffsetDelta != offsetDelta) {
				writeOffsetDelta();
				offsetDelta = newOffsetDelta;
				lastOffsetDeltaChange = offset;
			}

			for (int cc = 0; cc < text.length(); cc++) {
				final char currentChar = text.charAt(cc);
				if (!preserveSpace && configuration.isCompressingWhitespace() && Character.isWhitespace(lastChar)
						&& Character.isWhitespace(currentChar)) {
					continue;
				}
				textBuffer.write(Character.toString(lastChar = currentChar).getBytes(charset));
				offset++;
			}
		}

		private void end() {
			writeOffsetDelta();
		}

		private void writeOffsetDelta() {
			if (offsetDeltas != null && offset > lastOffsetDeltaChange) {
				newOffsetDeltaRange(offsetDeltas, new Range(lastOffsetDeltaChange, offset), offsetDelta);
			}
		}

		private void writeText() {
			if (startOffset >= 0 && offset > startOffset) {
				Layer text = startAnnotation(target, QNameImpl.TEXT_QNAME, Maps.<QName, String> newHashMap(),
						startOffset, nodePath);
				endAnnotation(text, offset);
			}
			startOffset = -1;
		}

		private Reader read() throws IOException {
			return new InputStreamReader(textBuffer.getSupplier().getInput(), charset);
		}

		private void dispose() throws IOException {
			textBuffer.reset();
		}
	}
}
