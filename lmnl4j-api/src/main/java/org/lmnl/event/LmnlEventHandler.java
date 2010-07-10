package org.lmnl.event;

import org.lmnl.LmnlAnnotation;
import org.lmnl.LmnlDocument;


public interface LmnlEventHandler {
	void startDocument(LmnlDocument document) throws LmnlEventHandlerException;

	void endDocument(LmnlDocument document) throws LmnlEventHandlerException;

	void startAnnotation(LmnlAnnotation annotation) throws LmnlEventHandlerException;

	void endAnnotation(LmnlAnnotation annotation) throws LmnlEventHandlerException;
}
