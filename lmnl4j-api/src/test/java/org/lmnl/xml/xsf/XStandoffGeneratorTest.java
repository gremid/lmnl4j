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

import java.io.StringWriter;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.lmnl.AbstractXmlTest;
import org.lmnl.xml.XmlElementAnnotation;
import org.w3c.dom.Document;

import com.google.common.base.Predicate;

/**
 * Tests generation of XStandoff markup.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class XStandoffGeneratorTest extends AbstractXmlTest {

	/**
	 * Generates a XStandoff document from a TEI-P5 source by isolating two
	 * annotation levels, one for segments (<code>&lt;seg/></code>) and one
	 * for <code>&lt;choice/></code> related markup.
	 * 
	 * @throws Exception
	 *                 in case a DOM/XSLT related error occurs
	 */
	@Test
	public void generateXsf() throws Exception {
		DocumentBuilder domBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document dom = domBuilder.newDocument();

		XStandoffMarkupGenerator xsf = new XStandoffMarkupGenerator(document(), dom);
		xsf.addLevel(new Predicate<XmlElementAnnotation>() {

			public boolean apply(XmlElementAnnotation input) {
				return "seg".equals(input.getLocalName());
			}

		});
		xsf.addLevel(new Predicate<XmlElementAnnotation>() {
			private Pattern names = Pattern.compile("(choice)|(abbr)|(expan)");

			public boolean apply(XmlElementAnnotation input) {
				return names.matcher(input.getLocalName()).matches();
			}

		});
		xsf.close();

		if (LOG.isLoggable(Level.FINE)) {
			StringWriter out = new StringWriter();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(2));
			transformer.transform(new DOMSource(dom), new StreamResult(out));
			printDebugMessage(out.toString());
		}
	}
}
