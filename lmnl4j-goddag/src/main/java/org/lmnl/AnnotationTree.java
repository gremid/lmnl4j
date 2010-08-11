package org.lmnl;

import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public enum AnnotationTree implements RelationshipType {
	CHILD, SIBLING, FIRST, LAST;
	
	public static long getRootId(Relationship r) {
		return (Long) r.getProperty("root", -1);
	}

	public static void setRootId(Relationship r, long id) {
		r.setProperty("root", id);		
	}
}
