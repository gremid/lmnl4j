package org.lmnl;

import java.io.IOException;
import java.io.Reader;
import java.util.SortedMap;
import java.util.SortedSet;

public interface TextRepository {

	int length(Annotation annotation) throws IOException;

	Reader read(Annotation annotation) throws IOException;

	String read(Annotation annotation, Range range) throws IOException;
	
	SortedMap<Range, String> bulkRead(Annotation annotation, SortedSet<Range> ranges) throws IOException;

}
