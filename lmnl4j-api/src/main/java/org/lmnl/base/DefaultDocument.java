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

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.lmnl.AnnotationFactory;
import org.lmnl.Document;
import org.lmnl.json.DocumentSerializer;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

@JsonSerialize(using = DocumentSerializer.class)
public class DefaultDocument extends AbstractLayer implements Document {
	protected BiMap<String, URI> namespaceContext = HashBiMap.create();
	private final AnnotationFactory annotationFactory;

	public DefaultDocument(URI id, String text, AnnotationFactory annotationFactory) {
		super(null, "lmnl", "document", text);
		this.annotationFactory = annotationFactory;
		setId(id);
		addNamespace(Document.LMNL_PREFIX, Document.LMNL_NS_URI);
	}

	public DefaultDocument(URI id, String text) {
		this(id, text, DEFAULT_FACTORY);
	}
	
	public BiMap<String, URI> getNamespaceContext() {
		return Maps.unmodifiableBiMap(namespaceContext);
	}

	public AnnotationFactory getAnnotationFactory() {
		return annotationFactory;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || (!(obj instanceof DefaultDocument))) {
			return super.equals(obj);
		}

		return getId().equals(((DefaultDocument) obj).getId());
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).addValue(getId()).toString();
	}
	
	public void addNamespace(String prefix, URI ns) {
		Preconditions.checkArgument(!namespaceContext.containsKey(prefix), prefix + " already mapped");
		namespaceContext.put(prefix, ns);
	}
	
	public URI getNamespace(String prefix) {
		return namespaceContext.get(prefix);
	}
	
	public String getPrefix(URI ns) {
		return namespaceContext.inverse().get(ns);
	}
	
	private static final AnnotationFactory DEFAULT_FACTORY = new DefaultAnnotationFactory();
}
