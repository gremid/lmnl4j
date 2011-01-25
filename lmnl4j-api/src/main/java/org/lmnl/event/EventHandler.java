package org.lmnl.event;

import org.lmnl.Annotation;
import org.lmnl.Document;


public interface EventHandler {
	void startDocument(Document document) throws EventHandlerException;

	void endDocument(Document document) throws EventHandlerException;

	void startAnnotation(Annotation annotation) throws EventHandlerException;

	void endAnnotation(Annotation annotation) throws EventHandlerException;
}
