package org.lmnl.util;

import java.util.HashMap;
import java.util.Map;

import org.lmnl.LmnlLayer;
import org.lmnl.LmnlRange;

/**
 * A default implementation of an identifier generator.
 */
public class DefaultIdGenerator implements IdGenerator {
	private Map<String, Integer> layerIds = new HashMap<String, Integer>();

	@Override
	public String next(LmnlRange range) {
		return ("seg_" + range.start + "_" + range.end);
	}

	@Override
	public String next(LmnlLayer layer) {
		String qName = layer.getQName().replace(":", "_");
		Integer id = layerIds.get(qName);
		if (id == null) {
			id = -1;
		}
		layerIds.put(qName, ++id);
		return qName + id;
	}

	@Override
	public void reset() {
		layerIds.clear();
	}
}
