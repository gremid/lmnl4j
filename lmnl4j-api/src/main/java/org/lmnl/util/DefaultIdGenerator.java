package org.lmnl.util;

import java.util.HashMap;
import java.util.Map;

import org.lmnl.Layer;
import org.lmnl.Range;

/**
 * A default implementation of an identifier generator.
 */
public class DefaultIdGenerator implements IdGenerator {
	private Map<String, Integer> layerIds = new HashMap<String, Integer>();

	public String next(Range range) {
		return ("seg_" + range.start + "_" + range.end);
	}

	public String next(Layer layer) {
		String qName = layer.getQName().replace(":", "_");
		Integer id = layerIds.get(qName);
		if (id == null) {
			id = -1;
		}
		layerIds.put(qName, ++id);
		return qName + id;
	}

	public void reset() {
		layerIds.clear();
	}
}
