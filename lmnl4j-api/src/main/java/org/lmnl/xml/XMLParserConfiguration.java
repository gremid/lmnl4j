package org.lmnl.xml;

import java.util.Set;

import org.lmnl.QName;

import com.google.common.collect.Sets;

public class XMLParserConfiguration {

	private Set<QName> lineElements = Sets.newHashSet();
	private Set<QName> containerElements = Sets.newHashSet();

	public Set<QName> getLineElements() {
		return lineElements;
	}

	public void setLineElements(Set<QName> lineElements) {
		this.lineElements = lineElements;
	}

	public Set<QName> getContainerElements() {
		return containerElements;
	}

	public void setContainerElements(Set<QName> containerElements) {
		this.containerElements = containerElements;
	}
}
