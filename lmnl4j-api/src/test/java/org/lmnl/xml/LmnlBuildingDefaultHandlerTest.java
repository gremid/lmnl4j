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

import org.junit.Test;
import org.lmnl.AbstractXmlTest;
import org.lmnl.LmnlDocument;

/**
 * Tests the generation of LOMs from XML sources.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class LmnlBuildingDefaultHandlerTest extends AbstractXmlTest {

	/**
	 * Builds a test LOM.
	 */
	@Test
	public void buildLom() {
		LmnlDocument document = document("george-algabal-tei.xml");
		printDebugMessage(document.getText());
		printDebugMessage(document);
	}
}
