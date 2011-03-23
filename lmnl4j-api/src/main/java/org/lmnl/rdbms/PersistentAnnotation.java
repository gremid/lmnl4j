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

package org.lmnl.rdbms;

import java.io.Serializable;

import org.lmnl.Annotation;
import org.lmnl.Range;

import com.google.common.base.Objects;

public class PersistentAnnotation extends PersistentLayer implements Annotation {
	protected Range range;
	protected Serializable serializableData;

	public Range getRange() {
		return range;
	}

	public void setRange(Range range) {
		this.range = range;
	}

	public Object getData() {
		return serializableData;
	}

	public Serializable getSerializableData() {
		return serializableData;
	}

	public void setSerializableData(Serializable serializableData) {
		this.serializableData = serializableData;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).addValue(getName()).addValue(getRange()).toString();
	}
}
