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

import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.lmnl.rdbms.RelationalLayerFactory;
import org.lmnl.xml.XMLParser;
import org.lmnl.xml.XMLParserConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Base class for tests working with documents generated from XML test resources.
 * 
 * @author <a href="http://gregor.middell.net/" title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
@Transactional
public abstract class AbstractXMLTest extends AbstractTest {
	protected static final URI TEI_NS = URI.create("http://www.tei-c.org/ns/1.0");

	/**
	 * Names of available XML test resources.
	 */
	protected static final SortedSet<String> RESOURCES = Sets.newTreeSet(Lists.newArrayList(//
			"archimedes-palimpsest-tei.xml", "george-algabal-tei.xml", "homer-iliad-tei.xml"));

	private Map<String, Layer> documents = Maps.newHashMap();

	private XMLParserConfiguration parserConfiguration = new XMLParserConfiguration();

	@Autowired
	private RelationalLayerFactory layerFactory;

	@Autowired
	private XMLParser xmlParser;

	@Before
	public void configureXMLParser() {
		parserConfiguration.addLineElement(new QNameImpl(TEI_NS, "lg"));
		parserConfiguration.addLineElement(new QNameImpl(TEI_NS, "l"));
		parserConfiguration.addLineElement(new QNameImpl(TEI_NS, "speaker"));
		parserConfiguration.addLineElement(new QNameImpl(TEI_NS, "stage"));
		parserConfiguration.addLineElement(new QNameImpl(TEI_NS, "div"));
		parserConfiguration.addLineElement(new QNameImpl(TEI_NS, "head"));
		parserConfiguration.addLineElement(new QNameImpl(TEI_NS, "fw"));
		parserConfiguration.addLineElement(new QNameImpl(TEI_NS, "p"));

		parserConfiguration.addContainerElement(new QNameImpl(TEI_NS, "text"));
		parserConfiguration.addContainerElement(new QNameImpl(TEI_NS, "div"));
		parserConfiguration.addContainerElement(new QNameImpl(TEI_NS, "lg"));
		parserConfiguration.addContainerElement(new QNameImpl(TEI_NS, "subst"));
		parserConfiguration.addContainerElement(new QNameImpl(TEI_NS, "choice"));
	}

	/**
	 * Returns a test document generated from the resource with the given name.
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
	protected synchronized Layer document(String resource) {
		try {
			if (RESOURCES.contains(resource) && !documents.containsKey(resource)) {

				final Layer xml = layerFactory.create(null, XML_LAYER_NAME, null, "");
				final URI uri = AbstractXMLTest.class.getResource("/" + resource).toURI();

				xmlParser.load(xml, new StreamSource(uri.toASCIIString()));

				final Layer text = layerFactory.create(xml, TEXT_LAYER_NAME, null, "");
				final Layer offsetDeltas = layerFactory.create(text, OFFSET_LAYER_NAME, null, null);
				xmlParser.parse(xml, text, offsetDeltas, parserConfiguration);

				documents.put(resource, xml);
			}
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}

		return documents.get(resource);
	}

	/**
	 * Returns a default test document.
	 * 
	 * @return the document generated from the first available test resource
	 */
	protected Layer document() {
		return document(RESOURCES.first());
	}
}
