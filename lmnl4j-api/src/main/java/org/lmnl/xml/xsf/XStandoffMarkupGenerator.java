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

package org.lmnl.xml.xsf;

import java.net.URI;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;

import javax.xml.XMLConstants;

import org.lmnl.lom.LmnlAnnotation;
import org.lmnl.lom.LmnlLayer;
import org.lmnl.lom.LmnlRange;
import org.lmnl.lom.LmnlRangeAddress;
import org.lmnl.xml.XmlNodeAddress;
import org.lmnl.xml.XmlNodeSourced;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Generates multi-leveled standoff-markup according to the conventions of the
 * <a href="http://www.xstandoff.net/" title="Homepage">XStandoff project</a>.
 * 
 * <p/>
 * 
 * As the XStandoff notation is XML-oriented and aimed at expressing
 * XML-specific node hierarchies, LMNL annotations, which shall become part of
 * an <i>annotation level</i> (in XStandoff terms), have to implement
 * {@link XmlNodeSourced} and thus have a valid XML node position relative to
 * each other.
 * 
 * <p/>
 * 
 * After instantiating a generator object, handing it the layer to be serialized
 * and the target DOM node, {@link #addLevel(Predicate)} can be called for every
 * <i>annotation level</i> to be created. Lastly a call to {@link #close()}
 * finalizes the generation by serializing references segment descriptors.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class XStandoffMarkupGenerator {
	private static final String XSTANDOFF_NS_URI = "http://www.xstandoff.net/2009/xstandoff/1.1";
	private static final String XSTANDOFF_SCHEMA_URI = "http://www.xstandoff.net/2009/xstandoff/1.1/xsf.xsd";
	private static final String XSTANDOFF_VERSION = "1.0";

	private final LmnlLayer source;
	private final Node target;
	private final Document targetDocument;
	private final BiMap<String, LmnlRangeAddress> segments = HashBiMap.create();
	private XStandoffSegmentIdGenerator segmentIdGenerator = XStandoffSegmentIdGenerator.DEFAULT;
	private Element corpusData;
	private Element annotationRoot;

	/**
	 * Creates a new generator with source and target parameters.
	 * 
	 * @param source
	 *                the source layer, from which annotation will be
	 *                exported
	 * @param target
	 *                the DOM node to which the XStandoff tree will be
	 *                serialized
	 */
	public XStandoffMarkupGenerator(LmnlLayer source, Node target) {
		this.source = source;
		this.target = target;
		this.targetDocument = (target instanceof Document ? (Document) target : target.getOwnerDocument());
	}

	/**
	 * Configures the a callback object to generate unique identifiers for
	 * segments.
	 * 
	 * @param segmentIdGenerator
	 *                the callback object
	 * @return this generator instance (for method chaining)
	 */
	public XStandoffMarkupGenerator withSegmentIdGenerator(XStandoffSegmentIdGenerator segmentIdGenerator) {
		this.segmentIdGenerator = segmentIdGenerator;
		return this;

	}

	/**
	 * Adds an annotation level to the XStandoff tree by filtering a set of
	 * range annotations, that comprise the contents of that level.
	 * 
	 * @param predicate
	 *                a predicate choosing the range annotations to be part
	 *                of the new level
	 * @return this generator instance (for method chaining)
	 */
	public XStandoffMarkupGenerator addLevel(Predicate<LmnlRange> predicate) {
		if (corpusData == null) {
			createCorpusDataContext();
		}
		Element level = targetDocument.createElementNS(XSTANDOFF_NS_URI, "xsf:level");
		annotationRoot.appendChild(level);

		Stack<ElementMapping> nesting = new Stack<ElementMapping>();
		nesting.push(new ElementMapping(null, level));

		SortedSet<XmlNodeSourced> levelContents = Sets.newTreeSet(Iterables.filter(Iterables.filter(Iterables.filter(source.getAnnotations(), LmnlRange.class), predicate),
				XmlNodeSourced.class));
		for (XmlNodeSourced nodeSourced : levelContents) {
			final LmnlRange lmnlElement = (LmnlRange) nodeSourced;
			final XmlNodeAddress addr = nodeSourced.getXmlNodeAddress();

			while (nesting.peek().lmnl != null && !nesting.peek().lmnl.address().encloses(lmnlElement.address())) {
				nesting.pop();
			}
			while (nesting.peek().lmnl != null) {
				LmnlRange ancestor = nesting.peek().lmnl;
				if (!((XmlNodeSourced) ancestor).getXmlNodeAddress().isAncestorOf(addr)) {
					nesting.pop();
				} else {
					break;
				}
			}

			Element domElement = targetDocument.createElementNS(lmnlElement.getNamespace().toASCIIString(), lmnlElement.getQName());
			nesting.peek().dom.appendChild(domElement);

			for (XmlNodeSourced xmlAttr : Iterables.filter(lmnlElement.getAnnotations(), XmlNodeSourced.class)) {
				LmnlAnnotation lmnlAttr = (LmnlAnnotation) xmlAttr;
				if (lmnlAttr.getPrefix().length() == 0) {
					domElement.setAttribute(lmnlAttr.getQName(), lmnlAttr.getText());
				} else {
					domElement.setAttributeNS(lmnlAttr.getNamespace().toASCIIString(), lmnlAttr.getQName(), lmnlAttr.getText());
				}
			}
			if (lmnlElement.getId() != null) {
				domElement.setAttributeNS(XMLConstants.XML_NS_URI, "xml:id", lmnlElement.getId());
			}

			String segmentId = null;
			if (segments.containsValue(lmnlElement.address())) {
				segmentId = segments.inverse().get(lmnlElement);
			} else {
				segmentId = segmentIdGenerator.nextSegmentId(lmnlElement.address());
				segments.put(segmentId, new LmnlRangeAddress(lmnlElement.address()));
			}
			domElement.setAttributeNS(XSTANDOFF_NS_URI, "xsf:segment", "#" + segmentId);

			nesting.push(new ElementMapping(lmnlElement, domElement));
		}
		return this;
	}

	/**
	 * Has to be called at the end of XStandoff markup generation to
	 * serialize referenced segments.
	 */
	public void close() {
		Element segmentation = targetDocument.createElementNS(XSTANDOFF_NS_URI, "xsf:segmentation");

		BiMap<LmnlRangeAddress, String> segments = this.segments.inverse();
		for (LmnlRangeAddress segment : Sets.newTreeSet(segments.keySet())) {
			Element segmentEl = targetDocument.createElementNS(XSTANDOFF_NS_URI, "xsf:segment");
			segmentEl.setAttributeNS(XSTANDOFF_NS_URI, "xsf:type", "char");
			segmentEl.setAttributeNS(XSTANDOFF_NS_URI, "xsf:start", Integer.toString(segment.start));
			segmentEl.setAttributeNS(XSTANDOFF_NS_URI, "xsf:end", Integer.toString(segment.end));
			segmentEl.setAttributeNS(XMLConstants.XML_NS_URI, "xml:id", segments.get(segment));
			segmentation.appendChild(segmentEl);
		}

		corpusData.insertBefore(segmentation, annotationRoot);
	}

	private void createCorpusDataContext() {
		corpusData = targetDocument.createElementNS(XSTANDOFF_NS_URI, "xsf:corpusData");
		corpusData.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		corpusData.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi:schemaLocation", XSTANDOFF_NS_URI + " " + XSTANDOFF_SCHEMA_URI);
		corpusData.setAttributeNS(XSTANDOFF_NS_URI, "xsf:version", XSTANDOFF_VERSION);
		target.appendChild(corpusData);

		String text = source.getText();

		Element primaryData = targetDocument.createElementNS(XSTANDOFF_NS_URI, "xsf:primaryData");
		primaryData.setAttributeNS(XSTANDOFF_NS_URI, "xsf:start", Long.toString(0));
		primaryData.setAttributeNS(XSTANDOFF_NS_URI, "xsf:end", Long.toString(text.length()));

		Element textualContent = targetDocument.createElementNS(XSTANDOFF_NS_URI, "xsf:textualContent");
		textualContent.setTextContent(text);
		primaryData.appendChild(textualContent);

		corpusData.appendChild(primaryData);

		annotationRoot = targetDocument.createElementNS(XSTANDOFF_NS_URI, "xsf:annotation");
		Map<String, URI> namespaceContext = source.getNamespaceContext();
		for (String prefix : namespaceContext.keySet()) {
			String uri = namespaceContext.get(prefix).toASCIIString();
			if (prefix.length() == 0) {
				annotationRoot.setAttribute(XMLConstants.XMLNS_ATTRIBUTE, uri);
			} else {
				annotationRoot.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE + ":" + prefix, uri);
			}
		}
		corpusData.appendChild(annotationRoot);
	}

	private static class ElementMapping {
		private final LmnlRange lmnl;
		private final Element dom;

		private ElementMapping(LmnlRange lmnl, Element dom) {
			this.lmnl = lmnl;
			this.dom = dom;
		}
	}
}
