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

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * XML filter compressing whitespace and inserting newlines to tidy up
 * text-centric XML documents.
 * 
 * <p/>
 * 
 * The filter is configurable with regard to the set of elements, whose contents
 * are supposed to end with a newline and the set of "structural" elements, that
 * do not contain relevant textual content.
 * 
 * @see #withElementOnlyElements(Set)
 * @see #withLineElements(Set)
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class PlainTextXmlFilter extends XMLFilterImpl {
	private static final Pattern WHITESPACE = Pattern.compile("\\s+");
	private Set<String> lineElements = new HashSet<String>();
	private Set<String> elementOnlyElements = new HashSet<String>();
	private StringBuffer characterBuf = new StringBuffer();
	private Stack<String> localNames = new Stack<String>();
	private boolean lastCharsWithWhitespace = false;

	/**
	 * Create a filter instance with a default XML Reader as its parent.
	 * 
	 * @see XMLReaderFactory#createXMLReader()
	 * @throws SAXException
	 *                 if an XML-related error occurs while creating the
	 *                 default XML reader
	 */
	public PlainTextXmlFilter() throws SAXException {
		super(XMLReaderFactory.createXMLReader());
	}

	/**
	 * Creates a filter instance with the given reader as its parent.
	 * 
	 * @param parent
	 *                the parent reader
	 */
	public PlainTextXmlFilter(XMLReader parent) {
		super(parent);
	}

	/**
	 * Configures the set of elements, that represent lines and whose
	 * content therefore should end with a newline.
	 * 
	 * @param lineElements
	 *                a set of local names identifying line elements
	 * @return this filter instance (for method chaining)
	 */
	public PlainTextXmlFilter withLineElements(Set<String> lineElements) {
		this.lineElements = lineElements;
		return this;
	}

	/**
	 * Configures the set of elements, which do not contain textual content
	 * and whose whitespace-only text nodes can be savely discarded.
	 * 
	 * @param elementOnlyElements
	 *                a set of local names identifying "structural"
	 *                elements, whose textual content is comprised of
	 *                whitespace only (for example as a means of
	 *                indentation)
	 * @return this filter instance (for method chaining)
	 */
	public PlainTextXmlFilter withElementOnlyElements(Set<String> elementOnlyElements) {
		this.elementOnlyElements = elementOnlyElements;
		return this;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		characters();
		localNames.push(localName);
		super.startElement(uri, localName, qName, atts);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		characters();
		localNames.pop();

		if (lineElements.contains(localName)) {
			super.characters(new char[] { '\n' }, 0, 1);
			lastCharsWithWhitespace = true;
		}
		super.endElement(uri, localName, qName);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		characterBuf.append(ch, start, length);
	}

	private void characters() throws SAXException {
		if (characterBuf.length() == 0) {
			return;
		}

		String str = WHITESPACE.matcher(characterBuf).replaceAll(" ");
		characterBuf.setLength(0);

		if (" ".equals(str) && !localNames.isEmpty() && elementOnlyElements.contains(localNames.peek())) {
			return;
		}

		if (lastCharsWithWhitespace) {
			int start = 0;
			while (start < str.length() && Character.isWhitespace(str.charAt(start))) {
				start++;
			}
			str = str.substring(start, str.length());
		}
		super.characters(str.toCharArray(), 0, str.length());
		lastCharsWithWhitespace = str.endsWith(" ");
	}
}
