package org.lmnl.rdbms;

import java.sql.Clob;
import java.util.Set;

import org.lmnl.Annotation;
import org.lmnl.Text;

import com.google.common.base.Objects;

public class TextRelation implements Text {
	private int id;
	private Set<Annotation> annotations;
	private Clob content;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Set<Annotation> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(Set<Annotation> annotations) {
		this.annotations = annotations;
	}

	public Clob getContent() {
		return content;
	}

	public void setContent(Clob content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("id", Integer.toString(id)).toString();
	}
}
