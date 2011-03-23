package org.lmnl.event;

import org.lmnl.Annotation;


public interface EventHandler {
	void startAnnotation(Annotation annotation, int depth) throws EventHandlerException;

	void endAnnotation(Annotation annotation, int depth) throws EventHandlerException;
}
