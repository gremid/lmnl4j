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

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lmnl.Annotation;
import org.lmnl.Document;
import org.lmnl.Layer;
import org.lmnl.Range;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public abstract class AbstractLayer implements Layer {
	protected Layer owner;
	protected URI id;
	protected String prefix;
	protected String localName;
	protected String text;
	protected List<Annotation> annotations = new ArrayList<Annotation>();

	protected AbstractLayer(Layer owner, String prefix, String localName, String text) {
		this.owner = owner;
		this.prefix = prefix;
		this.localName = localName;
		this.text = text;
	}

	public Document getDocument() {
		for (Layer current = this; current != null; current = current.getOwner()) {
			if (current instanceof Document) {
				return (Document) current;
			}
		}
		throw new IllegalStateException();
	}

	public Layer getOwner() {
		return owner;
	}

	public URI getId() {
		return id;
	}

	public void setId(URI id) {
		this.id = id;
	}

	public URI getUri() {
		if (id == null) {
			return null;
		}
		Document document = getDocument();
		return (document == null ? id : (document.getId() == null ? id : document.getId().resolve(id)));
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public String getQName() {
		return (prefix == null || localName == null) ? null : (prefix.length() == 0 ? localName : prefix + ":" + localName);
	}

	public URI getNamespace() {
		return getDocument().getNamespaceContext().get(prefix);
	}

	public String getText() {
		return (text == null ? (owner == null ? null : owner.getText()) : text);
	}

	public boolean hasText() {
		return (text != null);
	}

	public void setText(String text) {
		this.text = text;
	}

	public Iterator<Annotation> iterator() {
		return annotations.iterator();
	}

	public <T extends Annotation> T add(String prefix, String localName, String text, Range address, Class<T> type) {
		Preconditions.checkArgument(getDocument().getNamespaceContext().containsKey(prefix), prefix + " not mapped");
		
		final T annotation = getDocument().getAnnotationFactory().create(this, prefix, localName, text, address, type);
		annotations.add(annotation);
		return annotation;
	}

	public <T extends Annotation> T add(T annotation, Class<T> type) {
		return add(annotation.getPrefix(), annotation.getLocalName(), annotation.getText(), annotation.address(), type);
	}
	
	public void destroy() {
		owner = null;
		annotations = null;
	}
	
	public void remove(Annotation annotation) {
		Preconditions.checkArgument(equals(annotation.getOwner()), annotation + " not a child of " + this);
		Preconditions.checkArgument(annotations.remove(annotation), annotation + " not a child of " + this);
		
		for (Annotation child : annotation) {
			annotation.remove(child);
		}
		
		getDocument().getAnnotationFactory().destroy(annotation);
	}

	public void visit(Visitor visitor) {
		for (Annotation a : annotations) {
			visitor.visit(a);
		}
		for (Annotation a : annotations) {
			a.visit(visitor);
		}
	}

	public <T extends Annotation> Iterable<T> select(Class<T> annotationType) {
		return Iterables.filter(this, annotationType);
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
