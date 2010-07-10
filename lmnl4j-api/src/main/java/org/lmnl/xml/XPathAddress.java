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

import org.codehaus.jackson.JsonGenerator;

/**
 * A pointer to an XML node, allowing it to be addressed.
 * 
 * <p/>
 * 
 * The address is comprised of an optional URI denoting the source document and
 * an array of integer values encoding the path to the XML node starting from
 * the source document's root element. Each path component equals the absolute
 * node position along the sibling axis of DOM hierarchy level, counting from
 * <code>1</code>. The node path of an attribute has trailing component, which
 * equals <code>0</code>, thereby distinguishing it from an element's path.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class XPathAddress implements Comparable<XPathAddress> {
	private final int[] address;

	/**
	 * Creates a pointer to an XML node.
	 * 
	 * @param address
	 *                the node's path
	 */
	public XPathAddress(int[] address) {
		this.address = address;
	}

	/**
	 * Checks, whether this address points to a node, that is an ancestor of
	 * another one.
	 * 
	 * @param other
	 *                the potential descendant node
	 * @return <code>true</code>/<code>false</code>
	 */
	public boolean isAncestorOf(XPathAddress other) {
		final int[] otherAddress = other.address;
		for (int ai = 0; ai < address.length; ai++) {
			if (ai == otherAddress.length || address[ai] != otherAddress[ai]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Yields an XPath 2.0 expression for the XML node address.
	 * 
	 * @return an XPath 2.0 expression based on the source document URI and
	 *         the numeric node path
	 */
	public String xpath(URI document) {
		StringBuilder xpath = new StringBuilder(document == null ? "" : ("fn:doc('" + document.toASCIIString() + "')"));
		xpath.append("/element()[1]");
		for (int pos : address) {
			xpath.append("/" + (pos == 0 ? "attribute()" : "*[" + pos + "]"));
		}
		return xpath.toString();
	}

	/**
	 * Prints a JSON representation of this XML node address.
	 * 
	 * @param jg
	 *                a streaming-oriented callback object to generate
	 *                output in <i>JavaScript Object Notation</i>
	 * @throws IOException
	 *                 in case an I/O related error occurs while streaming;
	 *                 propagated from the generator methods
	 */
	public void serialize(JsonGenerator jg) throws IOException {
		jg.writeStartObject();
		jg.writeArrayFieldStart("pos");
		for (int pos : address) {
			jg.writeNumber(pos);
		}
		jg.writeEndArray();
		jg.writeEndObject();
	}

	@Override
	public String toString() {
		return xpath(null);
	}

	@Override
	public int compareTo(XPathAddress o) {
		int i = 0;
		int oi = 0;

		while (i < address.length && oi < o.address.length) {
			int compareTo = address[i++] - o.address[oi++];
			if (compareTo != 0) {
				return compareTo;
			}
		}

		return (i < address.length ? 1 : (oi < o.address.length ? -1 : 0));
	}
}
