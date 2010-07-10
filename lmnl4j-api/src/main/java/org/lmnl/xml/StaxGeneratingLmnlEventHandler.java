package org.lmnl.xml;

import java.net.URI;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.lmnl.LmnlAnnotation;
import org.lmnl.LmnlDocument;
import org.lmnl.LmnlRange;
import org.lmnl.event.LmnlEventHandler;
import org.lmnl.event.LmnlEventHandlerException;

public class StaxGeneratingLmnlEventHandler implements LmnlEventHandler {

	private final XMLStreamWriter out;
	private boolean withText = true;
	private int level = 0;
	private int startOffset = 0;

	public StaxGeneratingLmnlEventHandler(XMLStreamWriter out) {
		this.out = out;
	}

	public StaxGeneratingLmnlEventHandler withText(boolean withText) {
		this.withText = withText;
		return this;
	}

	@Override
	public void startAnnotation(LmnlAnnotation annotation) throws LmnlEventHandlerException {
		try {
			if (level == 1) {
				text(annotation, annotation.address().start);
				out.writeStartElement(annotation.getPrefix(), annotation.getLocalName(), annotation.getNamespace().toASCIIString());
				for (LmnlAnnotation attr : annotation) {
					if (attr.hasText()) {
						if (attr.getPrefix() == null || attr.getPrefix().length() == 0) {
							out.writeAttribute(attr.getLocalName(), attr.getText());
						} else {
							out.writeAttribute(attr.getPrefix(), attr.getNamespace().toASCIIString(), attr.getLocalName(), attr.getText());
						}
					}
				}

			}
			level++;
		} catch (XMLStreamException e) {
			throw new LmnlEventHandlerException(e);
		}

	}

	@Override
	public void startDocument(LmnlDocument document) throws LmnlEventHandlerException {
		try {
			out.writeStartElement(document.getPrefix(), document.getLocalName(), document.getNamespace().toASCIIString());
			Map<String, URI> nsCtx = document.getNamespaceContext();
			for (String prefix : nsCtx.keySet()) {
				out.writeNamespace(prefix, nsCtx.get(prefix).toASCIIString());
			}

			level++;
		} catch (XMLStreamException e) {
			throw new LmnlEventHandlerException(e);
		}
	}

	@Override
	public void endAnnotation(LmnlAnnotation annotation) throws LmnlEventHandlerException {
		try {
			if (level == 1) {
				text(annotation, annotation.address().end);
				out.writeEndElement();
			}
			level--;
		} catch (XMLStreamException e) {
			throw new LmnlEventHandlerException(e);
		}
	}

	@Override
	public void endDocument(LmnlDocument document) throws LmnlEventHandlerException {
		try {
			out.writeEndElement();
			level--;
		} catch (XMLStreamException e) {
			throw new LmnlEventHandlerException(e);
		}
	}

	private void text(LmnlAnnotation annotation, int endOffset) throws XMLStreamException {
		if (withText && (startOffset < endOffset)) {
			out.writeCharacters(new LmnlRange(startOffset, endOffset).applyTo(annotation.getText()));
			startOffset = endOffset;
		}
	}
}
