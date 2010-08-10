package org.lmnl;

import static org.lmnl.AnnotationTree.CHILD;
import static org.lmnl.AnnotationTree.SIBLING;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.helpers.Predicate;
import org.neo4j.helpers.collection.FilteringIterable;
import org.neo4j.helpers.collection.IterableWrapper;
import org.neo4j.util.NodeWrapperImpl;

public class AnnotationNode extends NodeWrapperImpl implements Iterable<AnnotationNode> {
	public static final long SELF_OWNED = -1;
	protected final AnnotationNodeFactory nodeFactory;
	private long owner;

	public AnnotationNode(AnnotationNodeFactory nodeFactory, Node node, long owner) {
		super(node);
		this.nodeFactory = nodeFactory;
		this.owner = (owner < 0 ? node.getId() : owner);
	}

	public AnnotationNodeFactory getNodeFactory() {
		return nodeFactory;
	}

	public long getOwner() {
		return owner;
	}

	public Node getOwnerNode() {
		return getUnderlyingNode().getGraphDatabase().getNodeById(owner);
	}

	public void copyProperties(AnnotationNode other) {
	}

	public AnnotationNode getNextSibling() {
		Relationship nextSibling = siblingRelationshipOf(getUnderlyingNode(), owner, OUTGOING);
		return (nextSibling == null ? null : nodeFactory.wrapNode(nextSibling.getEndNode(), owner));
	}

	public AnnotationNode getPreviousSibling() {
		Relationship previousSibling = siblingRelationshipOf(getUnderlyingNode(), owner, INCOMING);
		return (previousSibling == null ? null : nodeFactory.wrapNode(previousSibling.getStartNode(), owner));
	}

	public AnnotationNode getFirstChild() {
		for (Relationship r : childRelationshipsOf(getUnderlyingNode(), owner)) {
			if (!r.getEndNode().hasRelationship(SIBLING, INCOMING)) {
				return nodeFactory.wrapNode(r.getEndNode(), owner);
			}
		}
		return null;
	}

	public AnnotationNode getLastChild() {
		for (Relationship r : childRelationshipsOf(getUnderlyingNode(), owner)) {
			if (siblingRelationshipOf(r.getEndNode(), owner, OUTGOING) == null) {
				return nodeFactory.wrapNode(r.getEndNode(), owner);
			}
		}
		return null;
	}

	@Override
	public Iterator<AnnotationNode> iterator() {
		return new IterableWrapper<AnnotationNode, Relationship>(childRelationshipsOf(getUnderlyingNode(), owner)) {

			@Override
			protected AnnotationNode underlyingObjectToObject(Relationship object) {
				return nodeFactory.wrapNode(object.getEndNode(), owner);
			}
		}.iterator();
	}

	public Iterable<AnnotationNode> getChildNodes() {
		List<AnnotationNode> children = new LinkedList<AnnotationNode>();
		for (AnnotationNode child : this) {
			children.add(child);
		}
		return children;
	}

	public AnnotationNode getParentNode() {
		for (Relationship r : getUnderlyingNode().getRelationships(CHILD, INCOMING)) {
			if (r.getProperty("owner").equals(owner)) {
				return nodeFactory.wrapNode(r.getStartNode(), owner);
			}
		}
		return null;
	}

	public AnnotationNode add(AnnotationNode newChild) {
		return insert(newChild, null);
	}

	public AnnotationNode insert(AnnotationNode newChild, AnnotationNode before) {
		if (before != null && !getUnderlyingNode().equals(before.getParentNode().getUnderlyingNode())) {
			throw new IllegalArgumentException(before.toString());
		}

		final Node newChildNode = newChild.getUnderlyingNode();
		newChild = nodeFactory.wrapNode(newChildNode, owner);

		AnnotationNode oldParent = newChild.getParentNode();
		if (oldParent != null) {
			oldParent.remove(newChild, false);
		}

		if (before == null) {
			AnnotationNode lastChild = getLastChild();
			if (lastChild != null) {
				Relationship siblingRel = lastChild.getUnderlyingNode().createRelationshipTo(newChildNode, SIBLING);
				siblingRel.setProperty("owner", owner);
			}
		} else {
			Node nextNode = before.getUnderlyingNode();
			Node prevNode = null;

			Relationship prevRel = siblingRelationshipOf(nextNode, owner, INCOMING);
			if (prevRel != null) {
				prevNode = prevRel.getStartNode();
				prevRel.delete();
			}

			Relationship nextSiblingRel = newChildNode.createRelationshipTo(nextNode, SIBLING);
			nextSiblingRel.setProperty("owner", owner);

			if (prevNode != null) {
				Relationship prevSiblingRel = prevNode.createRelationshipTo(newChildNode, SIBLING);
				prevSiblingRel.setProperty("owner", owner);
			}
		}
		Relationship childRel = getUnderlyingNode().createRelationshipTo(newChildNode, CHILD);
		childRel.setProperty("owner", owner);

		return newChild;
	}

	public void remove(AnnotationNode toRemove, boolean recursive) {
		if (!getUnderlyingNode().equals(toRemove.getParentNode().getUnderlyingNode())) {
			throw new IllegalArgumentException(toRemove.toString());
		}

		final Node nodeToRemove = toRemove.getUnderlyingNode();
		Relationship prev = siblingRelationshipOf(nodeToRemove, owner, INCOMING);
		Relationship next = siblingRelationshipOf(nodeToRemove, owner, OUTGOING);
		if (prev != null && next != null) {
			Relationship newSiblingRel = prev.getStartNode().createRelationshipTo(next.getEndNode(), SIBLING);
			newSiblingRel.setProperty("owner", owner);

		}
		if (prev != null) {
			prev.delete();
		}
		if (next != null) {
			next.delete();
		}
		for (Relationship r : nodeToRemove.getRelationships(CHILD, INCOMING)) {
			if (r.getProperty("owner").equals(owner)) {
				r.delete();
			}
		}
		if (recursive) {
			for (AnnotationNode child : toRemove.getChildNodes()) {
				toRemove.remove(child, recursive);
			}
			if (!nodeToRemove.hasRelationship()) {
				nodeToRemove.delete();
			}
		}
	}

	public void multiply(Predicate<AnnotationNode> filter, AnnotationNode to) {
		if (filter.accept(this)) {
			to = to.add(this);
		}
		for (AnnotationNode child : this) {
			child.multiply(filter, to);
		}
	}

	@Override
	public String toString() {
		return getClass().toString() + "[" + getUnderlyingNode().toString() + "]";
	}
	
	protected static Iterable<Relationship> childRelationshipsOf(Node node, final long owner) {
		return new FilteringIterable<Relationship>(node.getRelationships(CHILD, OUTGOING), new Predicate<Relationship>() {

			@Override
			public boolean accept(Relationship item) {
				return item.getProperty("owner").equals(owner);
			}
		});
	}

	protected static Relationship siblingRelationshipOf(Node node, final long owner, Direction direction) {
		for (Relationship r : node.getRelationships(SIBLING, direction)) {
			if (r.getProperty("owner").equals(owner)) {
				return r;
			}
		}
		return null;
	}
}
