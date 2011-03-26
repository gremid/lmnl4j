package org.lmnl.rdbms;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Collections.singleton;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.SortedMap;
import java.util.SortedSet;

import org.lmnl.Layer;
import org.lmnl.Range;
import org.lmnl.TextRepository;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
		return getOnlyElement(getText(layer, Sets.newTreeSet(singleton(range))).values());
	}

	public int getTextLength(Layer layer) throws IOException {
		try {
			return (int) layerFactory.getText(layer).getContent().length();
		} catch (SQLException e) {
			throw Throwables.propagate(e);
		}
	}

	public SortedMap<Range, String> getText(Layer layer, SortedSet<Range> ranges) throws IOException {
		try {
			final Clob text = layerFactory.getText(layer).getContent();
			final SortedMap<Range, String> results = Maps.newTreeMap();
			for (Range range : ranges) {
				results.put(range, text.getSubString(range.getStart() + 1, range.length()));
			}
			return results;
		} catch (SQLException e) {
			throw Throwables.propagate(e);
		}
	}
}
