package org.lmnl.rdbms;

import java.sql.Clob;

import org.lmnl.Text;

import com.google.common.base.Objects;

public class TextRelation implements Text {
	private int id;
	private Clob content;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
