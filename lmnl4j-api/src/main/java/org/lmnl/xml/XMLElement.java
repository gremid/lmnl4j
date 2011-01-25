/**
 * 
 */
package org.lmnl.xml;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.lmnl.Layer;
import org.lmnl.Range;
import org.lmnl.base.DefaultAnnotation;
import org.lmnl.json.XMLElementSerializer;

@JsonSerialize(using = XMLElementSerializer.class)
public class XMLElement extends DefaultAnnotation {
	protected XPath xPathAddress;
	protected Set<XMLAttribute> attributes = new HashSet<XMLAttribute>();

	protected XMLElement(Layer owner, String prefix, String localName, String text, Range range) {
		super(owner, prefix, localName, text, range);
	}

	public XPath getXPathAddress() {
		return xPathAddress;
	}
	
	public void setXPathAddress(XPath xPathAddress) {
		this.xPathAddress = xPathAddress;
	}
	
	public Set<XMLAttribute> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(Set<XMLAttribute> attributes) {
		this.attributes = attributes;
	}
}