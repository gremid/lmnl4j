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
import org.lmnl.lom.LmnlDocument;

public class DefaultLmnlDocument extends AbstractLmnlLayer implements LmnlDocument {
	protected URI base;

	public DefaultLmnlDocument(URI base) {
		super(LMNL_NS_URI, "lmnl", "document", null);
		this.base = base;
	}

	@Override
	public URI getBase() {
		return base;
	}

	@Override
	public String getId() {
		return base.toASCIIString();
	}

	@Override
	public void setId(String id) {
		this.base = URI.create(id);
	}

	@Override
	public URI getUri() {
		return base;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || (!(obj instanceof DefaultLmnlDocument))) {
			return super.equals(obj);
		}

		return base.equals(((DefaultLmnlDocument) obj).base);
	}

	@Override
	public int hashCode() {
		return base.hashCode();
	}

	@Override
	protected void serializeAttributes(JsonGenerator jg) throws IOException {
	}
}
