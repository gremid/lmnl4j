package org.lmnl;

import java.io.IOException;
import java.io.Reader;

public interface TextRepository {

	Text setText(Layer layer, Reader reader) throws IOException;

	Reader getText(Layer layer) throws IOException;

	String getText(Layer layer, Range range) throws IOException;

}
