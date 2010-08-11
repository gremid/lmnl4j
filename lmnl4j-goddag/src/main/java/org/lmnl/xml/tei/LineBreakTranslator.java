package org.lmnl.xml.tei;

import org.lmnl.xml.XmlAnnotationNode;

public class LineBreakTranslator extends MilestoneTranslator {

	public LineBreakTranslator() {
		super("http://www.tei-c.org/ns/1.0", "line");
	}

	@Override
	protected Iterable<MilestoneInterval> findIntervals(XmlAnnotationNode from) {
		return null;
	}

}
