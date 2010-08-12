package org.lmnl;

import static org.lmnl.AnnotationTree.CHILD;
import static org.lmnl.AnnotationTree.FIRST;
import static org.lmnl.AnnotationTree.LAST;
import static org.lmnl.AnnotationTree.SIBLING;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
	private static final Collection<AnnotationNode> EMPTY_RESULT_SET = new HashSet<AnnotationNode>();

	protected final AnnotationNodeFactory nodeFactory;
	private final AnnotationNode root;
	private final TraversalDescription traversal;

	private TraversalDescription ancestors;
	private TraversalDescription descendants;
	private TraversalDescription nextSiblings;
	private TraversalDescription previousSiblings;

	public AnnotationNode(AnnotationNodeFactory nodeFactory, Node node, AnnotationNode root) {
		super(node);
		this.nodeFactory = nodeFactory;
		this.root = (root == null ? this : root);
		this.traversal = Traversal.description().depthFirst().filter(new NodeFactorySupportedPredicate());
		this.ancestors = traversal.expand(new RootedRelationshipExpander(CHILD, INCOMING));
		this.descendants = traversal.expand(new RootedRelationshipExpander(CHILD, OUTGOING));
		this.nextSiblings = traversal.expand(new RootedRelationshipExpander(SIBLING, OUTGOING));
		this.previousSiblings = traversal.expand(new RootedRelationshipExpander(SIBLING, INCOMING));
	}

	public AnnotationNodeFactory getNodeFactory() {
		return nodeFactory;
	}

	public AnnotationNode getRoot() {
		return root;
	}

	public AnnotationNode adopt(AnnotationNode node) {
		return nodeFactory.wrapNode(node.getUnderlyingNode(), getRoot());
	}

	public void copyProperties(AnnotationNode other) {
	}

	public Iterable<AnnotationNode> traverse(TraversalDescription traversal) {
		return new NodeFactoryBasedWrapper(traversal.traverse(getUnderlyingNode()).nodes());
	}

	public Iterable<AnnotationNode> walk() {
		return new NodeFactoryBasedWrapper(traversal.expand(new DocumentOrderRelationshipExpander())
				.traverse(getUnderlyingNode()).nodes());
	}

	public AnnotationNode getNextSibling() {
		Iterator<AnnotationNode> siblings = traverse(nextSiblings.prune(Traversal.pruneAfterDepth(1))//
				.filter(Traversal.returnAllButStartNode())).iterator();
		return (siblings.hasNext() ? siblings.next() : null);
	}

	public AnnotationNode getPreviousSibling() {
		Iterator<AnnotationNode> siblings = traverse(previousSiblings.prune(Traversal.pruneAfterDepth(1))//
				.filter(Traversal.returnAllButStartNode())).iterator();
		return (siblings.hasNext() ? siblings.next() : null);
	}

	public AnnotationNode getFirstChild() {
		Iterator<AnnotationNode> firstChildren = traverse(traversal//
				.expand(new RootedRelationshipExpander(FIRST, OUTGOING))//
				.prune(Traversal.pruneAfterDepth(1))//
				.filter(Traversal.returnAllButStartNode())).iterator();
		return (firstChildren.hasNext() ? firstChildren.next() : null);
	}

	public AnnotationNode getLastChild() {
		Iterator<AnnotationNode> lastChildren = traverse(traversal//
				.expand(new RootedRelationshipExpander(LAST, INCOMING))//
				.prune(Traversal.pruneAfterDepth(1))//
				.filter(Traversal.returnAllButStartNode())).iterator();
		return (lastChildren.hasNext() ? lastChildren.next() : null);
	}

	public Iterable<AnnotationNode> getPrecedingNodes() {
		final Set<Node> ancestorNodes = new HashSet<Node>();
		for (Node ancestor : ancestors.filter(Traversal.returnAllButStartNode()).traverse(getUnderlyingNode()).nodes()) {
			ancestorNodes.add(ancestor);
		}

		return new FilteringIterable<AnnotationNode>(getRoot().traverse(
				traversal.expand(new DocumentOrderRelationshipExpander())), new Predicate<AnnotationNode>() {
			boolean afterThisNode = false;

			@Override
			public boolean accept(AnnotationNode item) {
				if (AnnotationNode.this.equals(item)) {
					afterThisNode = true;
					return false;
				}
				return (!afterThisNode && !ancestorNodes.contains(item.getUnderlyingNode()));
			}
		});
	}

	public Iterable<AnnotationNode> getFollowingNodes() {
		final Set<Node> descendantNodes = new HashSet<Node>();
		for (Node descendant : descendants.filter(Traversal.returnAllButStartNode()).traverse(getUnderlyingNode()).nodes()) {
			descendantNodes.add(descendant);
		}

		return new FilteringIterable<AnnotationNode>(getRoot().traverse(
				traversal.expand(new DocumentOrderRelationshipExpander())), new Predicate<AnnotationNode>() {

			boolean afterThisNode = false;

			@Override
			public boolean accept(AnnotationNode item) {
				if (AnnotationNode.this.equals(item)) {
					afterThisNode = true;
					return false;
				}
				return (afterThisNode && !descendantNodes.contains(item.getUnderlyingNode()));
			}
		});
	}

	@Override
	public Iterator<AnnotationNode> iterator() {
		AnnotationNode firstChild = getFirstChild();
		if (firstChild == null) {
			return EMPTY_RESULT_SET.iterator();
		}
		return firstChild.traverse(nextSiblings).iterator();
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
		return traverse(ancestors.filter(Traversal.returnAllButStartNode()));
	}

	public AnnotationNode add(AnnotationNode newChild) {
		return insert(newChild, null);
	}

	public AnnotationNode insert(AnnotationNode newChild, AnnotationNode before) {
		if (before != null && !equals(before.getParentNode())) {
			throw new IllegalArgumentException(before.toString());
		}

		final Node newChildNode = newChild.getUnderlyingNode();
		newChild = adopt(newChild);

		AnnotationNode oldParent = newChild.getParentNode();
		if (oldParent != null) {
			oldParent.remove(newChild, false);
		}

		final long rootId = root.getUnderlyingNode().getId();
		if (before == null) {
			AnnotationNode lastChild = getLastChild();
			if (lastChild != null) {
				Node lastChildNode = lastChild.getUnderlyingNode();
				AnnotationTree.setRootId(lastChildNode.createRelationshipTo(newChildNode, SIBLING), rootId);
				singleRootedRelationshipOf(lastChildNode, LAST, OUTGOING).delete();
			}
			AnnotationTree.setRootId(newChildNode.createRelationshipTo(getUnderlyingNode(), LAST), rootId);
			AnnotationNode firstChild = getFirstChild();
			if (firstChild == null) {
				AnnotationTree.setRootId(getUnderlyingNode().createRelationshipTo(newChildNode, FIRST), rootId);
			}
		} else {
			Node nextNode = before.getUnderlyingNode();
			Node prevNode = null;
			Relationship prevRel = singleRootedRelationshipOf(nextNode, SIBLING, INCOMING);
			if (prevRel != null) {
				prevNode = prevRel.getStartNode();
				prevRel.delete();
			}

			AnnotationTree.setRootId(newChildNode.createRelationshipTo(nextNode, SIBLING), rootId);

			if (prevNode != null) {
				AnnotationTree.setRootId(prevNode.createRelationshipTo(newChildNode, SIBLING), rootId);
			} else {
				Relationship firstRel = singleRootedRelationshipOf(nextNode, FIRST, INCOMING);
				if (firstRel != null) {
					firstRel.delete();
				}
				AnnotationTree.setRootId(getUnderlyingNode().createRelationshipTo(newChildNode, FIRST), rootId);
			}
		}
		AnnotationTree.setRootId(getUnderlyingNode().createRelationshipTo(newChildNode, CHILD), rootId);

		return newChild;
	}

	public void remove(AnnotationNode toRemove, boolean recursive) {
		if (!equals(toRemove.getParentNode())) {
			throw new IllegalArgumentException(toRemove.toString());
		}

		final Node nodeToRemove = toRemove.getUnderlyingNode();
		final long rootId = root.getUnderlyingNode().getId();

		Relationship prev = singleRootedRelationshipOf(nodeToRemove, SIBLING, INCOMING);
		Relationship next = singleRootedRelationshipOf(nodeToRemove, SIBLING, OUTGOING);
		if (prev != null && next != null) {
			AnnotationTree.setRootId(prev.getStartNode().createRelationshipTo(next.getEndNode(), SIBLING), rootId);
		}
		Node prevNode = null;
		if (prev != null) {
			prevNode = prev.getStartNode();
			prev.delete();
		} else {
			singleRootedRelationshipOf(nodeToRemove, FIRST, INCOMING).delete();
			if (next != null) {
				AnnotationTree.setRootId(getUnderlyingNode().createRelationshipTo(next.getEndNode(), FIRST), rootId);
			}
		}

		if (next != null) {
			next.delete();
		} else {
			singleRootedRelationshipOf(nodeToRemove, LAST, OUTGOING).delete();
			if (prevNode != null) {
				AnnotationTree.setRootId(prevNode.createRelationshipTo(getUnderlyingNode(), LAST), rootId);
			}
		}
		singleRootedRelationshipOf(nodeToRemove, CHILD, INCOMING).delete();

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
	public boolean equals(Object o) {
		if (o != null && o instanceof AnnotationNode) {
			AnnotationNode other = (AnnotationNode) o;
			return getUnderlyingNode().equals(other.getUnderlyingNode())
					&& root.getUnderlyingNode().equals(other.root.getUnderlyingNode());
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = hash * 59 + root.getUnderlyingNode().hashCode();
		hash = hash * 59 + getUnderlyingNode().hashCode();
		return hash;
	}

	@Override
	public String toString() {
		return getClass().toString() + "[" + getUnderlyingNode().toString() + "]";
	}

	protected Relationship singleRootedRelationshipOf(Node node, RelationshipType rel, Direction direction) {
		Iterator<Relationship> relationships = rootedRelationshipsOf(node, rel, direction).iterator();
		return (relationships.hasNext() ? relationships.next() : null);
	}

	protected Iterable<Relationship> rootedRelationshipsOf(Node node, RelationshipType rel, Direction direction) {
		return new FilteringIterable<Relationship>(node.getRelationships(rel, direction), new Predicate<Relationship>() {
			private long rootId = root.getUnderlyingNode().getId();

			@Override
			public boolean accept(Relationship item) {
				return AnnotationTree.getRootId(item) == rootId;
			}
		});
	}

	protected class NodeFactoryBasedWrapper extends IterableWrapper<AnnotationNode, Node> {

		public NodeFactoryBasedWrapper(Iterable<Node> iterableToWrap) {
			super(iterableToWrap);
		}

		@Override
		protected AnnotationNode underlyingObjectToObject(Node object) {
			return nodeFactory.wrapNode(object, getRoot());
		}
	}

	protected class NodeFactorySupportedPredicate implements Predicate<Path> {

		@Override
		public boolean accept(Path item) {
			return nodeFactory.supports(item.endNode());
		}
	}

	protected class RootedRelationshipExpander implements RelationshipExpander {
		private final RelationshipType relationshipType;
		private final Direction direction;

		protected RootedRelationshipExpander(RelationshipType relationshipType, Direction direction) {
			this.relationshipType = relationshipType;
			this.direction = direction;
		}

		@Override
		public Iterable<Relationship> expand(Node node) {
			return new FilteringIterable<Relationship>(node.getRelationships(relationshipType, direction),
					new Predicate<Relationship>() {

						@Override
						public boolean accept(Relationship item) {
							return root.getUnderlyingNode().getId() == AnnotationTree.getRootId(item);
						}
					});
		}

		@Override
		public RelationshipExpander reversed() {
			return new RootedRelationshipExpander(relationshipType, direction.reverse());
		}
	}

	protected class DocumentOrderRelationshipExpander implements RelationshipExpander {
		private final Direction direction;
		private final long rootId;

		public DocumentOrderRelationshipExpander() {
			this(OUTGOING);
		}

		public DocumentOrderRelationshipExpander(Direction direction) {
			this.direction = direction;
			this.rootId = root.getUnderlyingNode().getId();
		}

		@Override
		public Iterable<Relationship> expand(Node node) {
			List<Relationship> expand = new ArrayList<Relationship>();

			for (Relationship firstChildRel : node.getRelationships(FIRST, direction)) {
				if (AnnotationTree.getRootId(firstChildRel) == rootId) {
					expand.add(firstChildRel);
				}
			}

			for (Relationship nextSiblingRel : node.getRelationships(SIBLING, direction)) {
				if (AnnotationTree.getRootId(nextSiblingRel) == rootId) {
					expand.add(nextSiblingRel);
				}
			}

			for (Relationship lastChildRel : node.getRelationships(LAST, direction)) {
				if (AnnotationTree.getRootId(lastChildRel) == rootId) {
					expand.add(lastChildRel);
				}

			}

			if (direction.equals(INCOMING)) {
				Collections.reverse(expand);
			}
			return expand;
		}

		@Override
		public RelationshipExpander reversed() {
			return new DocumentOrderRelationshipExpander(direction.reverse());
		}
	}
}
