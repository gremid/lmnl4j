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

import org.lmnl.Document;
import org.lmnl.Layer;
import org.lmnl.QName;

import com.google.common.base.Objects;

public abstract class PersistentLayer implements Layer {
	protected int id;
	protected Document document;
	protected Layer owner;
	protected QName name;
	protected PersistentText text;

	public PersistentLayer() {
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public Document getDocument() {
		return document;
	}
	
	public void setDocument(Document document) {
		this.document = document;
	}
	
	public Layer getOwner() {
		return owner;
	}

	public void setOwner(Layer owner) {
		this.owner = owner;
	}
	
	public QName getName() {
		return name;
	}
	
	public void setName(QName name) {
		this.name = name;
	}
	
	public PersistentText getText() {
		return text;
	}
	
	public void setText(PersistentText text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this).addValue(getName()).toString();
	}

	// FIXME: implement proper type-safe removal
	// public void remove(LmnlRange deleted) {
	// remove(deleted, true);
	// }
	//
	// protected void remove(LmnlRange deleted, boolean effectedLayer) {
	// for (Iterator<LmnlAnnotation> it = annotations.iterator();
	// it.hasNext();) {
	// LmnlAnnotation child = it.next();
	// if (!(child instanceof LmnlRange)) {
	// child.remove(deleted, false);
	// continue;
	// }
	// LmnlRange range = (LmnlRange) child;
	// if (range.congruentWith(deleted) || deleted.encloses(range)) {
	// it.remove();
	// continue;
	// }
	// if (deleted.overlapsWith(range)) {
	// child.remove(range.overlap(deleted).relativeTo(range), false);
	// }
	// child.setRange(range.substract(deleted));
	// }
	// if (effectedLayer && text != null) {
	// int length = text.length();
	// this.text = text.substring(0, Math.min(length, deleted.getStart())) +
	// text.substring(Math.min(deleted.getEnd(), length), length);
	// }
	// }
}
