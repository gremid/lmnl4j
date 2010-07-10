/**
 * 
 */
package org.lmnl.xml;

import java.io.IOException;
import java.net.URI;

import org.codehaus.jackson.JsonGenerator;
import org.lmnl.lom.LmnlRangeAddress;
import org.lmnl.lom.base.DefaultLmnlAnnotation;

public class XmlElement extends DefaultLmnlAnnotation implements XmlNodeSourced {
	protected XmlNodeAddress xmlNodeAddress;

	public XmlElement(URI uri, String prefix, String localName, int start) {
		super(uri, prefix, localName, null, new LmnlRangeAddress(start, start));
	}

	@Override
	public XmlNodeAddress getXmlNodeAddress() {
		return xmlNodeAddress;
	}

	public void setXmlNodeAddress(XmlNodeAddress xmlNodeAddress) {
		this.xmlNodeAddress = xmlNodeAddress;
	}

	@Override
	public int compareTo(XmlNodeSourced o) {
		return XmlNodeSourced.COMPARATOR.compare(this, o);
	}

	@Override
	protected void serializeAttributes(JsonGenerator jg) throws IOException {
		super.serializeAttributes(jg);
		jg.writeFieldName("xmlNode");
		xmlNodeAddress.serialize(jg);
	}

}