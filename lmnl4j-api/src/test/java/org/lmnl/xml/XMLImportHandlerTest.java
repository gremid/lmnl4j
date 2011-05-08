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
import static org.lmnl.xml.XMLParser.OFFSET_DELTA_NAME;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import org.junit.Test;
import org.lmnl.AbstractXMLTest;
import org.lmnl.Annotation;
import org.lmnl.AnnotationRepository;
import org.lmnl.QName;
import org.lmnl.Range;
import org.lmnl.TextContentReader;
import org.lmnl.TextRepository;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
	private AnnotationRepository annotationRepository;

	@Test
	public void showTextContents() throws IOException {
		// final Annotation document = document("homer-iliad-tei.xml");
		final Annotation document = document("george-algabal-tei.xml");
		final Annotation text = getOnlyElement(annotationRepository.find(document, TEXT_ANNOTATION_NAME));
		final Annotation offsets = getOnlyElement(annotationRepository.find(text, OFFSET_ANNOTATION_NAME));

		final int textLength = textRepository.length(text);
		assertTrue(textLength > 0);

		if (LOG.isDebugEnabled()) {
			final SortedMap<String, Annotation> annotations = Maps.newTreeMap();
			for (Annotation annotation : annotationRepository.find(text)) {
				@SuppressWarnings("unchecked")
				final Map<QName, String> attrs = (Map<QName, String>) annotation.getData();
				if (attrs == null) {
					LOG.debug(annotation + " has no attributes");
					continue;
				}
				final String nodePath = attrs.get(XMLParser.NODE_PATH_NAME);
				if (nodePath == null) {
					LOG.debug(annotation + " has no XML node path");
					continue;
				}
				if (annotations.containsKey(nodePath)) {
					LOG.debug(nodePath + " already assigned to " + annotations.get(nodePath));
				}
				annotations.put(nodePath, annotation);
			}
			for (Map.Entry<String, Annotation> annotation : annotations.entrySet()) {
				LOG.debug(annotation.getKey() + " ==> " + annotation.getValue());
			}

			if (LOG.isDebugEnabled()) {
				textRepository.read(text, new TextContentReader() {
					
					public void read(Reader content, int contentLength) throws IOException {
						LOG.debug(CharStreams.toString(content));
					}
				});
			}

			final SortedSet<Range> textRanges = Sets.newTreeSet();
			final SortedSet<Range> sourceRanges = Sets.newTreeSet();

			for (Annotation offset : annotationRepository.find(offsets, OFFSET_DELTA_NAME)) {
				final int delta = (Integer) offset.getData();
				final Range range = offset.getRange();
				textRanges.add(range);
				sourceRanges.add(range.add(delta));
			}

			final SortedMap<Range, String> texts = textRepository.bulkRead(text, textRanges);
			final SortedMap<Range, String> sources = textRepository.bulkRead(document, sourceRanges);

			final Iterator<Map.Entry<Range, String>> sourceIt = sources.entrySet().iterator();
			for (Map.Entry<Range, String> textRange : texts.entrySet()) {
				if (!sourceIt.hasNext()) {
					break;
				}
				LOG.debug(textRange.getValue() + " ==> " + sourceIt.next().getValue());
			}
		}

	}
}
