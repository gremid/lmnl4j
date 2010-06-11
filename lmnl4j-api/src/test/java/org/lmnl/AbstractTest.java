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
import java.util.List;
import java.util.SortedMap;

import org.codehaus.jackson.JsonGenerator;
import org.lmnl.lom.LmnlLayer;
import org.lmnl.lom.LmnlRange;
import org.lmnl.lom.LmnlRangeAddress;
import org.lmnl.lom.base.AbstractLmnlLayer;
import org.lmnl.lom.util.OverlapIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for tests providing utility functions.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public abstract class AbstractTest {
	/**
	 * A logger for debug output.
	 */
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractTest.class.getPackage().getName());

	/**
	 * Prints the given {@link OverlapIndexer range index} to the log.
	 * 
	 * @param index
	 *                the range index to output
	 */
	protected void printDebugMessage(SortedMap<LmnlRangeAddress, List<LmnlRange>> index) {
		if (LOG.isDebugEnabled()) {
			final StringBuilder str = new StringBuilder();
			for (LmnlRangeAddress segment : index.keySet()) {
				str.append("[" + segment + ": { ");
				boolean first = true;
				for (LmnlRange annotation : index.get(segment)) {
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

	/**
	 * Prints a JSON representation of the given layer to the log.
	 * 
	 * @param layer
	 *                the layer to print
	 */
	protected void printDebugMessage(LmnlLayer layer) {
		if (LOG.isDebugEnabled()) {
			try {
				StringWriter out = new StringWriter();
				JsonGenerator jg = AbstractLmnlLayer.JSON.createJsonGenerator(out);
				jg.useDefaultPrettyPrinter();
				layer.serialize(jg);
				jg.flush();
				LOG.debug(out.toString());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
