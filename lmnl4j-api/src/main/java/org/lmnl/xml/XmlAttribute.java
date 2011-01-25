/**
 * 
 */
package org.lmnl.xml;


public class XmlAttribute implements Comparable<XmlAttribute> {

	public final String prefix;
	public final String localName;
	public final String value;

	public XmlAttribute(String prefix, String localName, String value) {
		this.prefix = prefix;
		this.localName = localName;
		this.value = value;
	}

	public String getQName() {
		return (prefix.length() == 0 ? localName : (prefix + ":" + localName));
	}

	public int compareTo(XmlAttribute o) {
		return getQName().compareTo(o.getQName());
	}
}