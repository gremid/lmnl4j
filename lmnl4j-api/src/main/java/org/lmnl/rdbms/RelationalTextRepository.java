package org.lmnl.rdbms;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

import org.lmnl.Layer;
import org.lmnl.Range;
import org.lmnl.TextRepository;

import com.google.common.base.Throwables;

public class RelationalTextRepository implements TextRepository {

	private RelationalLayerFactory layerFactory;
	
	public void setLayerFactory(RelationalLayerFactory layerFactory) {
		this.layerFactory = layerFactory;
	}
	
	public Reader getText(Layer layer) throws IOException {
		try {
			return layerFactory.getText(layer).getContent().getCharacterStream();
		} catch (SQLException e) {
			throw Throwables.propagate(e);
		}
	}

	public String getText(Layer layer, Range range) throws IOException {
		try {
			return layerFactory.getText(layer).getContent().getSubString(range.getStart() + 1, range.length());
		} catch (SQLException e) {
			throw Throwables.propagate(e);
		}
	}

	public int getTextLength(Layer layer) throws IOException {
		try {
			return (int) layerFactory.getText(layer).getContent().length();
		} catch (SQLException e) {
			throw Throwables.propagate(e);
		}
	}
}
