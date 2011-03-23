package org.lmnl.event;

import org.lmnl.Layer;


public interface EventHandler {
	void startAnnotation(Layer annotation, int depth) throws EventHandlerException;

	void endAnnotation(Layer annotation, int depth) throws EventHandlerException;
}
