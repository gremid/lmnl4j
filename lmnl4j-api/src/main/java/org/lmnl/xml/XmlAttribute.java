/**
 * 
 */
package org.lmnl.xml;

import java.net.URI;

public class XmlAttribute implements Comparable<XmlAttribute> {

	public final URI ns;
	public final String prefix;
	public final String localName;
	public final String value;

	public XmlAttribute(URI ns, String prefix, String localName, String value) {
		this.ns = ns;
		this.prefix = prefix;
		this.localName = localName;
		this.value = value;
	}

	public String getQName() {
		return (prefix.length() == 0 ? localName : (prefix + ":" + localName));
	}

	@Override
	public int compareTo(XmlAttribute o) {
		return getQName().compareTo(o.getQName());
	}
}