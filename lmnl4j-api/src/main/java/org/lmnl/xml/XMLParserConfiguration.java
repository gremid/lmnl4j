package org.lmnl.xml;

import java.util.Set;

import org.lmnl.QName;

import com.google.common.collect.Sets;

public class XMLParserConfiguration {

	private Set<QName> excluded = Sets.newHashSet();
	private Set<QName> included = Sets.newHashSet();
	private Set<QName> lineElements = Sets.newHashSet();
	private Set<QName> containerElements = Sets.newHashSet();

	public void addLineElement(QName lineElementName) {
		lineElements.add(lineElementName);
	}

	public boolean removeLineElement(QName lineElementName) {
		return lineElements.remove(lineElementName);
	}

	public boolean isLineElement(QName name) {
		return lineElements.contains(name);
	}
	
	public void addContainerElement(QName containerElementName) {
		containerElements.add(containerElementName);
	}

	public boolean removeContainerElement(QName containerElementName) {
		return containerElements.remove(containerElementName);
	}

	public boolean isContainerElement(QName name) {
		return containerElements.contains(name);
	}
	
	public void include(QName name) {
		included.add(name);
	}
	
	public void exclude(QName name) {
		excluded.add(name);
	}
	
	public boolean included(QName name) {
		return included.contains(name);
	}
	
	public boolean excluded(QName name) {
		return excluded.contains(name);
	}
}
