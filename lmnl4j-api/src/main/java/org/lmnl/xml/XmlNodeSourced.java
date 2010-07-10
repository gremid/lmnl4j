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

import java.util.Comparator;

/**
 * Interface implemented by annotations, that have its source in some XML
 * document node and want to keep a precise reference to this node.
 * 
 * @see XmlNodeAddress
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public interface XmlNodeSourced extends Comparable<XmlNodeSourced> {

	/**
	 * Yields the address of the XML node, which served as the source for an
	 * annotation.
	 * 
	 * @return the XML node's address
	 */
	XmlNodeAddress getXmlNodeAddress();

	/**
	 * Assigns an XML node address to the implementing annotation, thereby
	 * associating it with its source.
	 * 
	 * @param address
	 *                the XML node's address
	 */
	void setXmlNodeAddress(XmlNodeAddress address);

	/**
	 * A comparator function, establishing an order of annotation based on
	 * the relative position of their XML source nodes within a common DOM.
	 */
	final Comparator<XmlNodeSourced> COMPARATOR = new Comparator<XmlNodeSourced>() {

		@Override
		public int compare(XmlNodeSourced o1, XmlNodeSourced o2) {
			return o1.getXmlNodeAddress().compareTo(o2.getXmlNodeAddress());
		}
	};

}
