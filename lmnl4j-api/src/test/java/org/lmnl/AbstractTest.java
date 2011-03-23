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

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;
import java.util.SortedMap;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.junit.runner.RunWith;
import org.lmnl.rdbms.PersistentText;
import org.lmnl.util.OverlapIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Base class for tests providing utility functions.
 * 
 * @author <a href="http://gregor.middell.net/" title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/testContext.xml")
public abstract class AbstractTest {
	/**
	 * Base URI of the in-memory document
	 */
	protected static final URI TEST_DOCUMENT_URI = URI.create("urn:lmnl-test");

	/**
	 * Test namespace prefix.
	 */
	protected static final String TEST_NS_PREFIX = "test";

	/**
	 * Test namespace.
	 */
	protected static final URI TEST_NS = URI.create("urn:lmnl-test-ns");

	/**
	 * A logger for debug output.
	 */
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractTest.class.getPackage().getName());

	protected ObjectMapper jsonMapper = new ObjectMapper();

	/**
	 * Prints the given {@link OverlapIndexer range index} to the log.
	 * 
	 * @param index
	 *                the range index to output
	 */
	protected void printDebugMessage(SortedMap<Range, List<Annotation>> index) {
		if (LOG.isDebugEnabled()) {
			final StringBuilder str = new StringBuilder();
			for (Range segment : index.keySet()) {
				str.append("[" + segment + ": { ");
				boolean first = true;
				for (Annotation annotation : index.get(segment)) {
					if (first) {
						first = false;
					} else {
						str.append(", ");
					}
					str.append(annotation.toString());
				}
				str.append(" }]\n");
			}
			LOG.debug(str.toString());
		}
	}

	/**
	 * Prints the given message to the log.
	 * 
	 * @param msg
	 *                the debug message
	 */
	protected void printDebugMessage(String msg) {
		LOG.debug(msg);
	}

	protected PersistentText createText(Session session, String text) {
		final PersistentText created = new PersistentText();
		created.setContent(Hibernate.createClob(text));
		session.save(created);
		session.flush();
		session.refresh(created);
		return created;
	}
}
