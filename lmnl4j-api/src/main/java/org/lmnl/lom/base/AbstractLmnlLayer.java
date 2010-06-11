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
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.lmnl.lom.LmnlAnnotation;
import org.lmnl.lom.LmnlDocument;
import org.lmnl.lom.LmnlLayer;

import com.google.common.collect.Sets;

public abstract class AbstractLmnlLayer implements LmnlLayer {
	public static final JsonFactory JSON = new JsonFactory();

	private LmnlLayer owner;
	private String id;
	private String prefix;
	private String localName;
	private String text;
	private List<LmnlAnnotation> annotations = new ArrayList<LmnlAnnotation>();
	private Map<String, URI> namespaceContext = new HashMap<String, URI>();

	protected AbstractLmnlLayer(URI uri, String prefix, String localName, String text) {
		this.prefix = prefix;
		this.localName = localName;
		this.text = text;
		this.namespaceContext.put(prefix, uri);
	}

	@Override
	public LmnlDocument getDocument() {
		LmnlLayer current = owner;
		while (owner != null && !(owner instanceof LmnlDocument)) {
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
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		// FIXME: check validity of fragment identifier
		this.id = id;
	}

	@Override
	public URI getUri() {
		LmnlDocument document = getDocument();
		if (document == null) {
			throw new IllegalStateException();
		}
		return (id == null ? null : document.getBase().resolve("#" + id));
	}

	@Override
	public Map<String, URI> getNamespaceContext() {
		return (namespaceContext == null ? owner.getNamespaceContext() : namespaceContext);
	}

	@Override
	public void setNamespaceContext(Map<String, URI> context) {
		this.namespaceContext = (context == null ? null : new HashMap<String, URI>(context));
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
		final Map<String, URI> ctx = this.getNamespaceContext();
		return (ctx == null ? null : ctx.get(prefix));
	}

	@Override
	public String getText() {
		return (text == null ? (owner == null ? null : owner.getText()) : text);
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
		Map<String, URI> ctx = getNamespaceContext();
		for (String commonPrefix : Sets.intersection(ctx.keySet(), annotation.getNamespaceContext().keySet())) {
			final URI existingUri = ctx.get(commonPrefix);
			final URI newUri = annotation.getNamespaceContext().get(commonPrefix);
			if (!existingUri.equals(newUri)) {
				// FIXME: remap clashing prefixes
				throw new IllegalArgumentException("Namespace clash on prefix '" + commonPrefix + "' : '" + existingUri + "' != '" + newUri + "'");
			}
		}
		ctx.putAll(annotation.getNamespaceContext());
		annotation.setNamespaceContext(null);
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
		annotation.setOwner(null);
		annotation.setNamespaceContext(new HashMap<String, URI>(getNamespaceContext()));
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
	
	public void serialize(JsonGenerator jg) throws IOException {
		jg.writeStartObject();
		if (owner == null) {
			if (!namespaceContext.isEmpty()) {
				jg.writeArrayFieldStart("ns");
				for (String prefix : namespaceContext.keySet()) {
					jg.writeStartObject();
					jg.writeStringField("prefix", prefix);
					jg.writeStringField("uri", namespaceContext.get(prefix).toString());
					jg.writeEndObject();
				}
				jg.writeEndArray();
			}
		}

		if (id != null) {
			jg.writeStringField("id", id);
		}

		jg.writeStringField("name", getQName());
		serializeAttributes(jg);
		if (text != null) {
			jg.writeStringField("text", text);
		}
		if (!annotations.isEmpty()) {
			jg.writeArrayFieldStart("annotations");
			for (LmnlAnnotation annotation : annotations) {
				annotation.serialize(jg);
			}
			jg.writeEndArray();
		}
		jg.writeEndObject();

	}

	protected abstract void serializeAttributes(JsonGenerator jg) throws IOException;

	@Override
	public String toString() {
		try {
			StringWriter str = new StringWriter();
			JsonGenerator jg = JSON.createJsonGenerator(str);
			serialize(jg);
			jg.flush();
			return str.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
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
