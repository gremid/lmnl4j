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

import org.junit.After;
import org.junit.Before;
import org.lmnl.base.DefaultLmnlAnnotation;
import org.lmnl.base.DefaultLmnlDocument;

/**
 * Base class for tests using an in-memory document model.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public abstract class AbstractDefaultLmnlDocumentTest extends AbstractTest {
	/**
	 * The in-memory document model to run tests against.
	 */
	protected LmnlDocument document;

	/**
	 * Creates a new document model before every test.
	 */
	@Before
	public void createDocument() {
		document = new DefaultLmnlDocument(TEST_DOCUMENT_URI, documentText());
		document.addNamespace(TEST_NS_PREFIX, TEST_NS);
	}

	/**
	 * Removes the document model.
	 */
	@After
	public void cleanDocument() {
		document = null;
	}

	/**
	 * Adds a simple {@link DefaultLmnlAnnotation annotation} to the test
	 * document.
	 * 
	 * @param name
	 *                the local name of the annotation (its namespace will
	 *                be {@link #TEST_NS})
	 * @param start
	 *                its start offset
	 * @param end
	 *                its end offset
	 */
	protected void addTestRange(String name, int start, int end) {
		document.add(TEST_NS_PREFIX, name, null, new LmnlRange(start, end), LmnlAnnotation.class);
	}

	/**
	 * Overridden by test classes to provide the textual contents of the
	 * test document.
	 * 
	 * @return the textual contents of the test document's layer
	 */
	protected abstract String documentText();
}
