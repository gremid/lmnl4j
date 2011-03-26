package org.lmnl.rdbms;

import java.sql.Clob;
import java.util.Set;

import org.lmnl.Layer;
import org.lmnl.Text;

import com.google.common.base.Objects;

public class TextRelation implements Text {
	private int id;
	private Set<Layer> layers;
	private Clob content;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Set<Layer> getLayers() {
		return layers;
	}

	public void setLayers(Set<Layer> layers) {
		this.layers = layers;
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
