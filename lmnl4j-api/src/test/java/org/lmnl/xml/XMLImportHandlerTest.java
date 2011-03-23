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

import static com.google.common.collect.Iterables.getOnlyElement;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.lmnl.AbstractXMLTest;
import org.lmnl.AnnotationFinder;
import org.lmnl.Layer;
import org.lmnl.Range;
import org.lmnl.TextRepository;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.io.CharStreams;

/**
 * Tests the generation of LOMs from XML sources.
 * 
 * @author <a href="http://gregor.middell.net/" title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class XMLImportHandlerTest extends AbstractXMLTest {

	@Autowired
	private TextRepository textRepository;

	@Autowired
	private AnnotationFinder annotationFinder;

	@Test
	public void showTextContents() throws IOException {
		//final Layer document = document("homer-iliad-tei.xml");
		final Layer document = document("george-algabal-tei.xml");
		final Layer text = getOnlyElement(annotationFinder.find(document, TEXT_LAYER_NAME, null));
		final Layer offsets = getOnlyElement(annotationFinder.find(text, OFFSET_LAYER_NAME, null));

		final int textLength = textRepository.getTextLength(text);
		assertTrue(textLength > 0);

		if (LOG.isDebugEnabled()) {
			LOG.debug(CharStreams.toString(textRepository.getText(text)));
		}
		for (Layer offset : annotationFinder.find(offsets, XMLParser.OFFSET_DELTA_NAME, null)) {
			if (LOG.isDebugEnabled()) {
				final int delta = (Integer) offset.getData();
				final Range range = offset.getRange();

				LOG.debug(textRepository.getText(text, range) + " ==> "
						+ textRepository.getText(document, range.add(delta)));
			}
		}
	}
}
