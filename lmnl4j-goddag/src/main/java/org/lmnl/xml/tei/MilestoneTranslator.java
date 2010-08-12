package org.lmnl.xml.tei;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.lmnl.AnnotationNode;
import org.lmnl.xml.Element;
import org.lmnl.xml.Text;
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

			XmlAnnotationNode start = null;
			for (AnnotationNode succ : interval.start.getFollowingNodes()) {
				if (succ instanceof Text) {
					start = (XmlAnnotationNode) succ;
					break;
				}
			}
			if (start == null) {
				continue;
			}
			XmlAnnotationNode end = null;
			for (AnnotationNode pred : interval.end.getPrecedingNodes()) {
				if (pred instanceof Text) {
					end = (XmlAnnotationNode) pred;
				}
			}
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

			Element element = createTranslatedElement(to, interval);

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

	protected Element createTranslatedElement(XmlAnnotationNode in, MilestoneInterval forInterval) {
		Element element = in.getNodeFactory().createNode(Element.class, in.getRoot());
		element.setNamespace(namespace);
		element.setName(name);
		return element;
	}

	protected abstract Iterable<MilestoneInterval> findIntervals(XmlAnnotationNode from);

	protected class MilestoneInterval {
		Element start;
		Element end;

		protected MilestoneInterval(Element start, Element end) {
			this.start = start;
			this.end = end;
		}
	}

	private Stack<XmlAnnotationNode> ancestorStack(XmlAnnotationNode node) {
		Stack<XmlAnnotationNode> ancestors = new Stack<XmlAnnotationNode>();
		for (XmlAnnotationNode ancestor : XmlAnnotationNodeFilter.filter(node.getAncestorNodes())) {
			ancestors.push(ancestor);
		}
		return ancestors;

	}

}
