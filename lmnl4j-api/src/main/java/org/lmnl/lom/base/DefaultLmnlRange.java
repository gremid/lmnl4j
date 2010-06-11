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

package org.lmnl.lom.base;

import java.io.IOException;
import java.net.URI;

import org.codehaus.jackson.JsonGenerator;
import org.lmnl.lom.LmnlRange;
import org.lmnl.lom.LmnlRangeAddress;

public class DefaultLmnlRange extends DefaultLmnlAnnotation implements LmnlRange {
	protected LmnlRangeAddress address;

	public DefaultLmnlRange(URI uri, String prefix, String localName, String text, int start, int end) {
		super(uri, prefix, localName, text);
		this.address = new LmnlRangeAddress(start, end);
	}

	@Override
	public LmnlRangeAddress address() {
		return address;
	}

	@Override
	public String getSegmentText() {
		return address.applyTo(getOwner().getText());
	}

	@Override
	protected void serializeAttributes(JsonGenerator jg) throws IOException {
		jg.writeArrayFieldStart("range");
		jg.writeNumber(address.start);
		jg.writeNumber(address.end);
		jg.writeEndArray();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj != null && (obj instanceof LmnlRange)) ? address.equals(((LmnlRange) obj).address()) : super.equals(obj);
	}

	@Override
	public int hashCode() {
		return address.hashCode();
	}
}
