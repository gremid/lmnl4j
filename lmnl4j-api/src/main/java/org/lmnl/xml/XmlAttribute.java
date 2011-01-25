/**
 * 
 */
package org.lmnl.xml;


public class XMLAttribute implements Comparable<XMLAttribute> {

	public final String prefix;
	public final String localName;
	public final String value;

	public XMLAttribute(String prefix, String localName, String value) {
		this.prefix = prefix;
		this.localName = localName;
		this.value = value;
	}

	public String getQName() {
		return (prefix.length() == 0 ? localName : (prefix + ":" + localName));
	}

	public int compareTo(XMLAttribute o) {
		return getQName().compareTo(o.getQName());
	}
}