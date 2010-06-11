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

package org.lmnl;

import java.net.URI;
import java.util.Map;
import java.util.SortedSet;

import org.junit.BeforeClass;
import org.lmnl.lom.base.DefaultLmnlDocument;
import org.lmnl.xml.PlainTextXmlFilter;
import org.lmnl.xml.SaxBasedLmnlBuilder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Base class for tests working with documents generated from XML test
 * resources.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public abstract class AbstractXmlSourcedTest extends AbstractTest {
	/**
	 * Names of available XML test resources.
	 */
	protected static final SortedSet<String> RESOURCES = Sets.newTreeSet(Lists.newArrayList(//
			"archimedes-palimpsest-tei.xml", "george-algabal-tei.xml", "homer-iliad-tei.xml"));

	private static Map<String, DefaultLmnlDocument> documents = Maps.newHashMap();
	private static SaxBasedLmnlBuilder builder;

	/**
	 * Creates a LOM builder for parsing XML test resources.
	 * 
	 * @throws SAXException
	 *                 if an XML related parser error occurs
	 */
	@BeforeClass
	public static void initBuilder() throws SAXException {
		builder = new SaxBasedLmnlBuilder(new PlainTextXmlFilter()//
				.withLineElements(Sets.newHashSet("lg", "l", "sp", "speaker", "stage", "div", "head", "p"))//
				.withElementOnlyElements(Sets.newHashSet("document", "surface", "zone", "subst")));
	}

	/**
	 * Returns a test document generated from the resource with the given
	 * name.
	 * 
	 * <p/>
	 * 
	 * The generated test document is cached for later reuse.
	 * 
	 * @param resource
	 *                the name of the resource
	 * @return the corresponding test document
	 * @see #RESOURCES
	 */
	protected synchronized DefaultLmnlDocument document(String resource) {
		try {
			if (RESOURCES.contains(resource) && !documents.containsKey(resource)) {
				URI uri = AbstractXmlSourcedTest.class.getResource("/" + resource).toURI();
				DefaultLmnlDocument document = new DefaultLmnlDocument(uri);
				builder.build(new InputSource(uri.toASCIIString()), document);
				documents.put(resource, document);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return documents.get(resource);
	}

	/**
	 * Returns a default test document.
	 * 
	 * @return the document generated from the first available test resource
	 */
	protected DefaultLmnlDocument document() {
		return document(RESOURCES.first());
	}
}
