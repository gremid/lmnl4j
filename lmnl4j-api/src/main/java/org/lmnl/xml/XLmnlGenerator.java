package org.lmnl.xml;

import java.net.URI;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.lmnl.lom.LmnlAnnotation;
import org.lmnl.lom.LmnlDocument;
import org.lmnl.lom.LmnlLayer;
import org.lmnl.lom.LmnlRangeAddress;

public class XLmnlGenerator {
	private static final String XLMNL_NS = "http://lmnl.org/namespace/LMNL/xLMNL";
	private final XMLStreamWriter out;

	public XLmnlGenerator(XMLStreamWriter out) {
		this.out = out;
	}

	public void generate(LmnlDocument document) throws XMLStreamException {
		generate(document, document);
	}

	public void generate(LmnlLayer layer, LmnlDocument document) throws XMLStreamException {
		Map<String, URI> ctx = document.getNamespaceContext();
		if (ctx.containsKey("x") && !XLMNL_NS.equals(ctx.get("x"))) {
			throw new IllegalArgumentException("Prefix 'x' already mapped to a difference NS in given layer");
		}
		out.writeStartElement("x", localName(layer), XLMNL_NS);
		if (!ctx.containsKey("x")) {
			out.writeNamespace("x", XLMNL_NS);
		}
		if (layer.equals(document)) {
			for (String prefix : ctx.keySet()) {
				out.writeNamespace(prefix, ctx.get(prefix).toASCIIString());
			}
		}
		if (layer.getId() != null) {
			out.writeAttribute("ID", layer.getId().toASCIIString());
		}
		out.writeAttribute("prefix", layer.getPrefix());
		out.writeAttribute("name", layer.getLocalName());
		if (layer instanceof LmnlAnnotation) {
			LmnlRangeAddress address = ((LmnlAnnotation) layer).address();
			// FIXME: identify XML attributes via subclass
			if (address.start != 0 || address.end != 0) {
				out.writeAttribute("start", Integer.toString(address.start));
				out.writeAttribute("length", Integer.toString(address.length()));
			}
		}
		if (layer.hasText()) {
			out.writeStartElement("x", "content", XLMNL_NS);
			out.writeCharacters(layer.getText());
			out.writeEndElement();
		}

		for (LmnlAnnotation child : layer) {
			generate(child, document);
		}
		out.writeEndElement();
	}

	private String localName(LmnlLayer layer) {
		if (layer instanceof LmnlDocument) {
			return "lmnl-document";
		}
		if (layer instanceof LmnlAnnotation) {
			return "annotation";
		}
		throw new IllegalArgumentException();
	}
}
