package org.lmnl;

import org.neo4j.graphdb.RelationshipType;

public enum AnnotationTree implements RelationshipType {
	CHILD, SIBLING
}
