package org.lmnl.xml.tei;

import java.util.ArrayList;
import java.util.List;

import org.lmnl.xml.Attribute;
import org.lmnl.xml.Element;
import org.lmnl.xml.XmlAnnotationNode;

public class LineBreakTranslator extends MilestoneTranslator {

	public LineBreakTranslator() {
		super("http://www.tei-c.org/ns/1.0", "line");
	}

	@Override
	protected Iterable<MilestoneInterval> findIntervals(XmlAnnotationNode from) {
		List<MilestoneInterval> intervals = new ArrayList<MilestoneInterval>();
		Element start = null;
		for (Object node : TeiUtil.newXPathContext(from).selectNodes("//tei:lb")) {
			Element end = (Element) node;
			if (start == null) {
				start = end;
				continue;
			}
			intervals.add(new MilestoneInterval(start, end));
			start = end;
		}
		return intervals;
	}

	@Override
	protected Element createTranslatedElement(XmlAnnotationNode in, MilestoneInterval forInterval) {
		Element lineElement = super.createTranslatedElement(in, forInterval);
		for (Attribute attribute : forInterval.start.getAttributes()) {
			lineElement.add(in.getNodeFactory().cloneNode(attribute));
		}
		return lineElement;
	}

}
