/**
 * 
 */
package org.lmnl.xml;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.lmnl.LmnlLayer;
import org.lmnl.LmnlRange;
import org.lmnl.base.DefaultLmnlAnnotation;
import org.lmnl.json.XmlElementAnnotationSerializer;

@JsonSerialize(using = XmlElementAnnotationSerializer.class)
public class XmlElementAnnotation extends DefaultLmnlAnnotation {
	protected XPathAddress xPathAddress;
	protected Set<XmlAttribute> attributes = new HashSet<XmlAttribute>();

	protected XmlElementAnnotation(LmnlLayer owner, String prefix, String localName, String text, LmnlRange range) {
		super(owner, prefix, localName, text, range);
	}

	public XPathAddress getXPathAddress() {
		return xPathAddress;
	}
	
	public void setXPathAddress(XPathAddress xPathAddress) {
		this.xPathAddress = xPathAddress;
	}
	
	public Set<XmlAttribute> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(Set<XmlAttribute> attributes) {
		this.attributes = attributes;
	}
}