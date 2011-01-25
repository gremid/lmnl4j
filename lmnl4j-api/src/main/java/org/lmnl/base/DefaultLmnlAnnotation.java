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

package org.lmnl.base;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.lmnl.LmnlAnnotation;
import org.lmnl.LmnlLayer;
import org.lmnl.LmnlRange;
import org.lmnl.json.LmnlAnnotationSerializer;

import com.google.common.base.Objects;

@JsonSerialize(using = LmnlAnnotationSerializer.class)
public class DefaultLmnlAnnotation extends AbstractLmnlLayer implements LmnlAnnotation {
	protected LmnlRange address;

	protected DefaultLmnlAnnotation(LmnlLayer owner, String prefix, String localName, String text, LmnlRange address) {
		super(owner, prefix, localName, text);
		this.address = address;
	}

	public LmnlRange address() {
		return address;
	}

	public String getSegmentText() {
		return address.applyTo(getOwner().getText());
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this).addValue(getQName()).addValue(address).toString();
	}
}
