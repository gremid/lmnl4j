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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.lmnl.LmnlAnnotation;
import org.lmnl.LmnlDocument;
import org.lmnl.LmnlLayer;

public abstract class AbstractLmnlLayer implements LmnlLayer {
	protected LmnlLayer owner;
	protected URI id;
	protected URI namespace;
	protected String prefix;
	protected String localName;
	protected String text;
	protected List<LmnlAnnotation> annotations = new ArrayList<LmnlAnnotation>();

	protected AbstractLmnlLayer(URI namespace, String prefix, String localName, String text) {
		this.namespace = namespace;
		this.prefix = prefix;
		this.localName = localName;
		this.text = text;
	}

	@Override
	public LmnlDocument getDocument() {		
		LmnlLayer current = this;
		while (current != null && !(current instanceof LmnlDocument)) {
			current = current.getOwner();
		}

		return (LmnlDocument) current;
	}

	@Override
	public LmnlLayer getOwner() {
		return owner;
	}

	@Override
	public void setOwner(LmnlLayer owner) {
		this.owner = owner;
	}

	@Override
	public URI getId() {
		return id;
	}

	@Override
	public void setId(URI id) {
		this.id = id;
	}

	@Override
	public URI getUri() {
		if (id == null) {
			return null;
		}
		LmnlDocument document = getDocument();
		return (document == null ? id : (document.getId() == null ? id : document.getId().resolve(id)));
	}

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String getLocalName() {
		return localName;
	}

	@Override
	public void setLocalName(String localName) {
		this.localName = localName;
	}

	@Override
	public String getQName() {
		return (prefix == null || localName == null) ? null : (prefix.length() == 0 ? localName : prefix + ":" + localName);
	}

	@Override
	public URI getNamespace() {
		return (namespace == null ? getDocument().getNamespaceContext().get(prefix) : namespace);
	}

	@Override
	public void setNamespace(URI namespace) {
		this.namespace = namespace;
	}

	@Override
	public String getText() {
		return (text == null ? (owner == null ? null : owner.getText()) : text);
	}

	@Override
	public boolean hasText() {
		return (text != null);
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public List<LmnlAnnotation> getAnnotations() {
		return Collections.unmodifiableList(annotations);
	}

	@Override
	public Iterator<LmnlAnnotation> iterator() {
		return new ArrayList<LmnlAnnotation>(getAnnotations()).iterator();
	}

	@Override
	public LmnlAnnotation add(LmnlAnnotation annotation) {
		if (annotation.getOwner() != null) {
			annotation.getOwner().remove(annotation);
		}

		String prefix = annotation.getPrefix();
		URI ns = annotation.getNamespace();
		Map<String, URI> ctx = getDocument().getNamespaceContext();
		if (ctx.containsValue(ns)) {
			if (ctx.containsKey(prefix)) {
				if (!ctx.get(prefix).equals(ns)) {
					throw new IllegalArgumentException("Prefix'" + prefix + "' already mapped: '" + ns + "' != '" + ctx.get(prefix) + "'");
				}				
			} else {
				for (String nsPrefix : ctx.keySet()) {
					if (ns.equals(ctx.get(nsPrefix))) {
						annotation.setPrefix(nsPrefix);
						break;
					}
				}
			}
		} else {
			ctx.put(prefix, ns);
		}

		annotation.setNamespace(null);
		annotation.setOwner(this);

		annotations.add(annotation);
		return annotation;
	}

	@Override
	public LmnlAnnotation remove(LmnlAnnotation annotation) {
		if (!equals(annotation.getOwner())) {
			return annotation;
		}
		if (!annotations.remove(annotation)) {
			throw new IllegalArgumentException(annotation.toString());
		}
		URI ns = annotation.getNamespace();
		annotation.setOwner(null);
		annotation.setNamespace(ns);
		return annotation;
	}

	@Override
	public void visit(Visitor visitor) {
		for (LmnlAnnotation a : annotations) {
			visitor.visit(a);
		}
		for (LmnlAnnotation a : annotations) {
			a.visit(visitor);
		}
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
