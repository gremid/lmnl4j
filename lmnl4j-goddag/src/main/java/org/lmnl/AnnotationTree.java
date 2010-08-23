package org.lmnl;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.helpers.collection.IterableWrapper;

public enum AnnotationTree implements RelationshipType {
    CHILD, SIBLING, FIRST, LAST;

    public static long getRootId(Relationship r) {
        return (Long) r.getProperty("root", -1);
    }

    public static void setRootId(Relationship r, long id) {
        r.setProperty("root", id);
    }

    public static Iterable<AnnotationNode> findAllTreeRoots(final AnnotationNode node) {
        final Node graphNode = node.getUnderlyingNode();

        Set<Long> rootIds = new HashSet<Long>();
        rootIds.add(node.getRoot().getUnderlyingNode().getId());
        for (Relationship treeRel : graphNode.getRelationships(values())) {
            rootIds.add(getRootId(treeRel));
        }
        
        return new IterableWrapper<AnnotationNode, Long>(rootIds) {
            private AnnotationNodeFactory nodeFactory = node.getNodeFactory();
            private GraphDatabaseService db = graphNode.getGraphDatabase();

            @Override
            protected AnnotationNode underlyingObjectToObject(Long object) {
                return nodeFactory.wrapNode(db.getNodeById(object), null);
            }
        };
    }
}
