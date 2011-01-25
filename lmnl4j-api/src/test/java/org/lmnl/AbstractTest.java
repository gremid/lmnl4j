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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.SortedMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.BeforeClass;
import org.lmnl.util.OverlapIndexer;

/**
 * Base class for tests providing utility functions.
 * 
 * @author <a href="http://gregor.middell.net/" title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
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
	protected static final Logger LOG = Logger.getLogger(AbstractTest.class.getPackage().getName());

	protected ObjectMapper jsonMapper = new ObjectMapper();

	/**
	 * Initializes logging for debug output during test execution.
	 */
	@BeforeClass
	public static void initLogging() {
		LOG.setUseParentHandlers(false);
		LOG.setLevel(Level.FINEST);

		for (Handler handler : LOG.getHandlers()) {
			if (handler instanceof ConsoleHandler) {
				LOG.removeHandler(handler);
			}
		}
		final ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new SimpleLogFormatter());
		handler.setLevel(Level.FINEST);
		LOG.addHandler(handler);
	}

	/**
	 * Prints the given {@link OverlapIndexer range index} to the log.
	 * 
	 * @param index
	 *                the range index to output
	 */
	protected void printDebugMessage(SortedMap<Range, List<Annotation>> index) {
		if (LOG.isLoggable(Level.FINE)) {
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
			LOG.fine(str.toString());
		}
	}

	/**
	 * Prints the given message to the log.
	 * 
	 * @param msg
	 *                the debug message
	 */
	protected void printDebugMessage(String msg) {
		LOG.fine(msg);
	}

	/**
	 * Prints a JSON representation of the given layer to the log.
	 * 
	 * @param layer
	 *                the layer to print
	 */
	protected void printDebugMessage(Layer layer) {
		if (LOG.isLoggable(Level.FINE)) {
			try {
				StringWriter out = new StringWriter();
				JsonGenerator jgen = jsonMapper.getJsonFactory().createJsonGenerator(out).useDefaultPrettyPrinter();
				jsonMapper.writeValue(jgen, layer);
				LOG.fine(out.toString());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static class SimpleLogFormatter extends Formatter {

		@Override
		public String format(LogRecord record) {
			final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
			StringBuilder msg = new StringBuilder();
			msg.append(String.format("[%s][%7s]: %s\n", df.format(record.getMillis()), record.getLevel(),
					record.getMessage()));
			if (record.getThrown() != null) {
				try {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					record.getThrown().printStackTrace(pw);
					pw.close();
					msg.append(sw.toString());
				} catch (Exception ex) {
				}
			}
			return msg.toString();
		}

	}

}
