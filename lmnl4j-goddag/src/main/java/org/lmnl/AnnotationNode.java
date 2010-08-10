package org.lmnl;

import static org.lmnl.AnnotationTree.CHILD;
import static org.lmnl.AnnotationTree.SIBLING;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipExpander;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.helpers.Predicate;
import org.neo4j.helpers.collection.FilteringIterable;
import org.neo4j.helpers.collection.IterableWrapper;
import org.neo4j.kernel.Traversal;
import org.neo4j.util.NodeWrapperImpl;

public class AnnotationNode extends NodeWrapperImpl implements Iterable<AnnotationNode> {
	public static final long SELF_OWNED = -1;
	private static final Collection<AnnotationNode> EMPTY_RESULT_SET = new HashSet<AnnotationNode>();

	protected final AnnotationNodeFactory nodeFactory;
	private final long owner;
	private final TraversalDescription traversal;

	public AnnotationNode(AnnotationNodeFactory nodeFactory, Node node, long owner) {
		super(node);
		this.nodeFactory = nodeFactory;
		this.owner = (owner < 0 ? node.getId() : owner);
		this.traversal = Traversal.description().filter(new NodeFactorySupportedPredicate());
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

	public Iterable<AnnotationNode> traverseWith(TraversalDescription traversal) {
		return new NodeFactoryBasedWrapper(traversal.traverse(getUnderlyingNode()).nodes());
	}

	public AnnotationNode getNextSibling() {
		Iterator<AnnotationNode> siblings = traverseWith(
				traversal.expand(new OwnedRelationshipExpander(SIBLING, OUTGOING))
						.prune(Traversal.pruneAfterDepth(1)).filter(Traversal.returnAllButStartNode()))
				.iterator();
		return (siblings.hasNext() ? siblings.next() : null);
	}

	public AnnotationNode getPreviousSibling() {
		Iterator<AnnotationNode> siblings = traverseWith(
				traversal.expand(new OwnedRelationshipExpander(SIBLING, INCOMING))
						.prune(Traversal.pruneAfterDepth(1)).filter(Traversal.returnAllButStartNode()))
				.iterator();
		return (siblings.hasNext() ? siblings.next() : null);
	}

	public AnnotationNode getFirstChild() {
		AnnotationNode firstChild = null;
		for (AnnotationNode childNode : traverseWith(traversal.depthFirst().prune(Traversal.pruneAfterDepth(1))
				.expand(new OwnedRelationshipExpander(CHILD, OUTGOING)).filter(Traversal.returnAllButStartNode()))) {
			firstChild = childNode;
			for (Relationship r : childNode.getUnderlyingNode().getRelationships(SIBLING, INCOMING)) {
				if (r.hasProperty("owner") && r.getProperty("owner").equals(owner)) {
					firstChild = null;
				}
			}
			if (firstChild != null) {
				break;
			}
		}
		return firstChild;
	}

	public AnnotationNode getLastChild() {
		AnnotationNode lastChild = null;
		for (AnnotationNode childNode : traverseWith(traversal.depthFirst().prune(Traversal.pruneAfterDepth(1))
				.expand(new OwnedRelationshipExpander(CHILD, OUTGOING)).filter(Traversal.returnAllButStartNode()))) {
			lastChild = childNode;
			for (Relationship r : childNode.getUnderlyingNode().getRelationships(SIBLING, OUTGOING)) {
				if (r.hasProperty("owner") && r.getProperty("owner").equals(owner)) {
					lastChild = null;
				}
			}
			if (lastChild != null) {
				break;
			}
		}
		return lastChild;
	}

	@Override
	public Iterator<AnnotationNode> iterator() {
		AnnotationNode firstChild = getFirstChild();
		return (firstChild == null ? EMPTY_RESULT_SET : firstChild.traverseWith(traversal.depthFirst().expand(
				new OwnedRelationshipExpander(SIBLING, OUTGOING)))).iterator();
	}

	public Iterable<AnnotationNode> getChildNodes() {
		List<AnnotationNode> children = new LinkedList<AnnotationNode>();
		for (AnnotationNode child : this) {
			children.add(child);
		}
		return children;
	}

	public AnnotationNode getParentNode() {
		Iterator<AnnotationNode> ancestors = getAncestorNodes().iterator();
		return (ancestors.hasNext() ? ancestors.next() : null);
	}

	public Iterable<AnnotationNode> getAncestorNodes() {
		return traverseWith(traversal.depthFirst().expand(new OwnedRelationshipExpander(CHILD, INCOMING))
				.filter(Traversal.returnAllButStartNode()));
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

	protected static Iterable<Relationship> childRelationshipsOf(Node node, final long owner, Direction direction) {
		return new FilteringIterable<Relationship>(node.getRelationships(CHILD, direction), new Predicate<Relationship>() {

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

	protected class NodeFactoryBasedWrapper extends IterableWrapper<AnnotationNode, Node> {

		public NodeFactoryBasedWrapper(Iterable<Node> iterableToWrap) {
			super(iterableToWrap);
		}

		@Override
		protected AnnotationNode underlyingObjectToObject(Node object) {
			return nodeFactory.wrapNode(object, owner);
		}
	}

	protected class NodeFactorySupportedPredicate implements Predicate<Path> {

		@Override
		public boolean accept(Path item) {
			return nodeFactory.supports(item.endNode());
		}
	}

	protected class OwnedRelationshipExpander implements RelationshipExpander {
		private final RelationshipType relationshipType;
		private final Direction direction;

		protected OwnedRelationshipExpander(RelationshipType relationshipType, Direction direction) {
			this.relationshipType = relationshipType;
			this.direction = direction;
		}

		@Override
		public Iterable<Relationship> expand(Node node) {
			return new FilteringIterable<Relationship>(node.getRelationships(relationshipType, direction),
					new Predicate<Relationship>() {

						@Override
						public boolean accept(Relationship item) {
							return item.hasProperty("owner") && item.getProperty("owner").equals(owner);
						}
					});
		}

		@Override
		public RelationshipExpander reversed() {
			return new OwnedRelationshipExpander(relationshipType, direction.reverse());
		}
	}
}
