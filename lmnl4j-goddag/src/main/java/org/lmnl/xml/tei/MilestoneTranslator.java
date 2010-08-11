package org.lmnl.xml.tei;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.lmnl.xml.Element;
import org.lmnl.xml.XmlAnnotationNode;
import org.lmnl.xml.XmlAnnotationNodeFilter;

public abstract class MilestoneTranslator {

	private final String namespace;
	private final String name;

	public MilestoneTranslator(String namespace, String name) {
		this.namespace = namespace;
		this.name = name;
	}

	public void translate(XmlAnnotationNode from, XmlAnnotationNode to) {
		for (MilestoneInterval interval : findIntervals(from)) {
			XmlAnnotationNode start = (XmlAnnotationNode) interval.startMilestone().newXPathContext().selectSingleNode("following::text()");
			if (start == null) {
				continue;
			}
			XmlAnnotationNode end = (XmlAnnotationNode) interval.endMilestone().newXPathContext().selectSingleNode("preceding::text()");
			if (end == null) {
				continue;
			}

			start = (XmlAnnotationNode) to.adopt(start);
			end = (XmlAnnotationNode) to.adopt(end);

			// FIXME: vary naive "Lowest common ancestor" algorithm
			Stack<XmlAnnotationNode> startAncestors = ancestorStack(start);
			Stack<XmlAnnotationNode> endAncestors = ancestorStack(end);
			XmlAnnotationNode lca = null;
			while (!startAncestors.isEmpty() && !endAncestors.isEmpty()
					&& startAncestors.peek().equals(endAncestors.peek())) {
				lca = startAncestors.pop();
				endAncestors.pop();
			}
			if (lca == null) {
				continue;
			}
			start = (startAncestors.isEmpty() ? start : startAncestors.peek());
			end = (endAncestors.isEmpty() ? end : endAncestors.peek());

			Element element = to.getNodeFactory().createNode(Element.class);
			element.setNamespace(namespace);
			element.setName(name);

			boolean collect = false;
			List<XmlAnnotationNode> childrenToMove = new LinkedList<XmlAnnotationNode>();
			for (XmlAnnotationNode child : XmlAnnotationNodeFilter.filter(lca)) {
				if (child.equals(start)) {
					collect = true;
				}
				if (collect) {
					childrenToMove.add(child);
				}
				if (child.equals(end)) {
					break;
				}
			}
			lca.insert(element, start);
			for (XmlAnnotationNode child : childrenToMove) {
				element.add(child);
			}
		}
	}

	private Stack<XmlAnnotationNode> ancestorStack(XmlAnnotationNode node) {
		Stack<XmlAnnotationNode> ancestors = new Stack<XmlAnnotationNode>();
		for (XmlAnnotationNode ancestor : XmlAnnotationNodeFilter.filter(node.getAncestorNodes())) {
			ancestors.push(ancestor);
		}
		return ancestors;

	}

	protected abstract Iterable<MilestoneInterval> findIntervals(XmlAnnotationNode from);

	public interface MilestoneInterval {
		public Element startMilestone();

		public Element endMilestone();
	}
}
