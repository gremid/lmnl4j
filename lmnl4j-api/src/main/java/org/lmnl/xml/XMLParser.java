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
import org.lmnl.QName;
import org.lmnl.QNameImpl;
import org.lmnl.Range;
import org.lmnl.TextContentReader;
import org.lmnl.TextRepository;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;

public abstract class XMLParser {
	public static final QName OFFSET_DELTA_NAME = new QNameImpl(Annotation.LMNL_NS_URI, "offset");
	public static final QName NODE_PATH_NAME = new QNameImpl(Annotation.LMNL_NS_URI, "xmlNode");

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

	public void load(Annotation annotation, Source xml) throws IOException, TransformerException {
		File sourceContents = File.createTempFile(getClass().getName(), ".xml");
		sourceContents.deleteOnExit();

		Reader sourceContentReader = null;

		try {
			final Transformer serializer = transformerFactory.newTransformer();
			serializer.setOutputProperty(OutputKeys.METHOD, "xml");
			serializer.setOutputProperty(OutputKeys.ENCODING, charset.name());
			serializer.setOutputProperty(OutputKeys.INDENT, "no");
			serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			serializer.transform(xml, new StreamResult(sourceContents));

			sourceContentReader = new InputStreamReader(new FileInputStream(sourceContents), charset);
			final int sourceContentLength = contentLength(sourceContentReader);
			sourceContentReader.close();

			sourceContentReader = new InputStreamReader(new FileInputStream(sourceContents), charset);
			updateText(annotation, sourceContentReader, sourceContentLength);
		} finally {
			Closeables.close(sourceContentReader, false);
			sourceContents.delete();
		}
	}

	public void parse(Annotation source, Annotation target, Annotation offsetDeltas, XMLParserConfiguration configuration)
			throws IOException, XMLStreamException {
		Session session = null;
		try {
			session = new Session(source, target, offsetDeltas, configuration);
			textRepository.read(source, session);
		} catch (Throwable t) {
			Throwables.propagateIfInstanceOf(t, IOException.class);
			Throwables.propagateIfInstanceOf(Throwables.getRootCause(t), XMLStreamException.class);
			Throwables.propagate(t);
		} finally {
			if (session != null) {
				session.dispose();
			}

		}
	}

	protected void updateText(Annotation annotation, Reader reader, int contentLength) throws IOException {
		textRepository.write(annotation, reader, contentLength);
	}

	protected abstract Annotation startAnnotation(Session session, QName name, Map<QName, String> attrs, int start,
			Iterable<Integer> nodePath);

	protected abstract void endAnnotation(Annotation annotation, int end);

	protected abstract void newOffsetDeltaRange(Session session, Range range, int offsetDelta);

	protected void newXMLEventBatch() {
	}

	private int contentLength(Reader reader) throws IOException {
		int contentLength = 0;
		while (reader.read() >= 0) {
			contentLength++;
		}
		return contentLength;
	}

	protected class Session implements TextContentReader {
		public final Annotation source;
		public final Annotation target;
		public final Annotation offsetDeltas;
		public final XMLParserConfiguration configuration;

		protected final Stack<Annotation> elementContext = new Stack<Annotation>();
		protected final Stack<Boolean> spacePreservationContext = new Stack<Boolean>();
		protected final Stack<Boolean> inclusionContext = new Stack<Boolean>();
		protected final Stack<Integer> nodePath = new Stack<Integer>();
		protected final FileBackedOutputStream textBuffer = new FileBackedOutputStream(textBufferSize);

		protected int offset = 0;
		protected int startOffset = -1;
		protected int offsetDelta = 0;
		protected int lastOffsetDeltaChange = 0;

		protected char notableCharacter;
		protected char lastChar = (removeLeadingWhitespace ? ' ' : 0);

		protected Session(Annotation source, Annotation target, Annotation offsetDeltas,
				XMLParserConfiguration configuration) {
			this.source = source;
			this.target = target;
			this.offsetDeltas = offsetDeltas;
			this.configuration = configuration;
			this.notableCharacter = configuration.getNotableCharacter();
			this.nodePath.push(0);
		}

		protected Annotation startElement(QName name, Map<QName, String> attributes) throws IOException {
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

			final Annotation annotation = startAnnotation(this, name, attributes, offset, nodePath);

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

		protected void endElement() throws IOException {
			nodePath.pop();
			spacePreservationContext.pop();
			inclusionContext.pop();
			endAnnotation(elementContext.pop(), offset);
		}

		protected void nextSibling() {
			nodePath.push(nodePath.pop() + 1);
		}

		protected void text(String text, int sourceOffset) throws IOException {
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

		protected void end() {
			writeOffsetDelta();
		}

		protected void writeOffsetDelta() {
			if (offset > lastOffsetDeltaChange) {
				newOffsetDeltaRange(this, new Range(lastOffsetDeltaChange, offset), offsetDelta);
			}
		}

		protected void writeText() {
			if (startOffset >= 0 && offset > startOffset) {
				Annotation text = startAnnotation(this, QNameImpl.TEXT_QNAME, Maps.<QName, String> newHashMap(),
						startOffset, nodePath);
				endAnnotation(text, offset);
			}
			startOffset = -1;
		}

		protected Reader read() throws IOException {
			return new InputStreamReader(textBuffer.getSupplier().getInput(), charset);
		}

		protected void dispose() throws IOException {
			textBuffer.reset();
		}

		public void read(Reader content, int contentLength) throws IOException {
			XMLStreamReader reader = null;
			try {
				reader = xmlInputFactory.createXMLStreamReader(content);
				int xmlEvents = 0;
				Map<QName, String> attributes = null;

				while (reader.hasNext()) {
					if (xmlEvents++ % xmlEventBatchSize == 0) {
						newXMLEventBatch();
					}

					switch (reader.next()) {
					case XMLStreamConstants.START_ELEMENT:
						writeText();
						nextSibling();

						attributes = Maps.newHashMap();
						final int attributeCount = reader.getAttributeCount();
						for (int ac = 0; ac < attributeCount; ac++) {
							final javax.xml.namespace.QName attrQName = reader.getAttributeName(ac);
							if (XMLNS_ATTRIBUTE_NS_URI.equals(attrQName.getNamespaceURI())) {
								continue;
							}
							attributes.put(new QNameImpl(attrQName), reader.getAttributeValue(ac));
						}

						startElement(new QNameImpl(reader.getName()), attributes);
						break;
					case XMLStreamConstants.END_ELEMENT:
						writeText();
						endElement();
						break;
					case XMLStreamConstants.COMMENT:
						writeText();
						nextSibling();

						attributes = Maps.newHashMap();
						attributes.put(QNameImpl.COMMENT_TEXT_QNAME, reader.getText());
						startElement(QNameImpl.COMMENT_QNAME, attributes);
						endElement();
						break;
					case XMLStreamConstants.PROCESSING_INSTRUCTION:
						writeText();
						nextSibling();

						attributes = Maps.newHashMap();
						attributes.put(QNameImpl.PI_TARGET_QNAME, reader.getPITarget());
						final String data = reader.getPIData();
						if (data != null) {
							attributes.put(QNameImpl.PI_DATA_QNAME, data);
						}

						startElement(QNameImpl.PI_QNAME, attributes);
						endElement();
						break;
					case XMLStreamConstants.CHARACTERS:
					case XMLStreamConstants.ENTITY_REFERENCE:
					case XMLStreamConstants.CDATA:
						text(reader.getText(), reader.getLocation().getCharacterOffset());
						break;
					case XMLStreamConstants.END_DOCUMENT:
						end();
						break;
					}
				}

				Reader textContentReader = null;
				try {
					textContentReader = read();
					final int textContentLength = contentLength(textContentReader);
					Closeables.close(textContentReader, false);

					textContentReader = read();
					updateText(target, textContentReader, textContentLength);
				} finally {
					Closeables.closeQuietly(textContentReader);
				}
			} catch (XMLStreamException e) {
				throw new RuntimeException(e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (XMLStreamException e) {
					}
				}
			}
		}
	}
}
