package org.lmnl.lom.util;

import java.util.HashMap;
import java.util.Map;

import org.lmnl.lom.LmnlLayer;
import org.lmnl.lom.LmnlRangeAddress;

/**
 * A default implementation of an identifier generator.
 */
public class DefaultIdGenerator implements IdGenerator {
	private Map<String, Integer> layerIds = new HashMap<String, Integer>();

	@Override
	public String next(LmnlRangeAddress range) {
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
