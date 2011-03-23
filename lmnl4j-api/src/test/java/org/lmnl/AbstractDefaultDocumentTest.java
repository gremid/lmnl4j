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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.lmnl.rdbms.PersistentAnnotation;
import org.lmnl.rdbms.PersistentDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for tests using an in-memory document model.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
@Transactional
public abstract class AbstractDefaultDocumentTest extends AbstractTest {
	@Autowired
	protected SessionFactory sessionFactory;
	
	@Autowired
	protected QNameRepository nameRepository;
	
	/**
	 * The in-memory document model to run tests against.
	 */
	protected PersistentDocument document;

	/**
	 * Creates a new document model before every test.
	 */
	@Before
	public void createDocument() {
		final Session session = sessionFactory.getCurrentSession();

		document = new PersistentDocument();
		document.setName(nameRepository.get(Document.LMNL_NS_URI, "document"));
		document.setText(createText(session, documentText()));
		session.save(document);
	}

	/**
	 * Removes the document model.
	 */
	@After
	public void cleanDocument() {
		document = null;
	}

	/**
	 * Adds a simple {@link PersistentAnnotation annotation} to the test
	 * document.
	 * 
	 * @param name
	 *                the local name of the annotation (its namespace will
	 *                be {@link #TEST_NS})
	 * @param start
	 *                its start offset
	 * @param end
	 *                its end offset
	 * @return 
	 */
	protected Annotation addTestRange(String name, int start, int end) {
		PersistentAnnotation annotation = new PersistentAnnotation();
		annotation.setDocument(document);
		annotation.setOwner(document);
		annotation.setName(nameRepository.get(TEST_NS, name));
		annotation.setRange(new Range(start, end));
		annotation.setText(document.getText());
		sessionFactory.getCurrentSession().save(annotation);
		return annotation;
	}

	/**
	 * Overridden by test classes to provide the textual contents of the
	 * test document.
	 * 
	 * @return the textual contents of the test document's layer
	 */
	protected abstract String documentText();
}
