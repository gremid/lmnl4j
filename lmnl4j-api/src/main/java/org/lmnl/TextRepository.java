package org.lmnl;

import java.io.IOException;
import java.io.Reader;
import java.util.SortedMap;
import java.util.SortedSet;

public interface TextRepository {

	int length(Annotation annotation) throws IOException;

	void read(Annotation annotation, TextContentReader reader) throws IOException;

	String read(Annotation annotation, Range range) throws IOException;
	
	SortedMap<Range, String> bulkRead(Annotation annotation, SortedSet<Range> ranges) throws IOException;

	void write(Annotation annotation, Reader contents, int contentLength) throws IOException;
}
