package org.lmnl;

import java.io.IOException;
import java.io.Reader;
import java.util.SortedMap;
import java.util.SortedSet;

public interface TextRepository {

	int getTextLength(Layer layer) throws IOException;

	Reader getText(Layer layer) throws IOException;

	String getText(Layer layer, Range range) throws IOException;
	
	SortedMap<Range, String> getText(Layer layer, SortedSet<Range> ranges) throws IOException;

}
