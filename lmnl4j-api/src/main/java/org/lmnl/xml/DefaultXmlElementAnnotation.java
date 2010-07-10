/**
 * 
 */
package org.lmnl.xml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.XMLConstants;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.lmnl.LmnlRange;
import org.lmnl.base.DefaultLmnlAnnotation;
import org.lmnl.json.XmlElementAnnotationSerializer;
import org.xml.sax.Attributes;

@JsonSerialize(using = XmlElementAnnotationSerializer.class)
public class DefaultXmlElementAnnotation extends DefaultLmnlAnnotation implements XmlElementAnnotation {
	protected XPathAddress xPathAddress;
	protected SortedSet<XmlAttribute> attributes = new TreeSet<XmlAttribute>();

	public DefaultXmlElementAnnotation(URI uri, String prefix, String localName, Attributes attrs, XPathAddress xPathAddress, int start) {
		super(uri, prefix, localName, null, new LmnlRange(start, start));
		this.xPathAddress = xPathAddress;
		for (int ac = 0; ac < attrs.getLength(); ac++) {
			String attrQName = attrs.getQName(ac);
			String attrUri = attrs.getURI(ac);
			if (attrQName.startsWith(XMLConstants.XMLNS_ATTRIBUTE) || XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attrUri)) {
				continue;
			}

			final String attrPrefix = attrQName.contains(":") ? attrQName.substring(0, attrQName.indexOf(":")) : "";
			final String attrLocalName = attrs.getLocalName(ac);
			final String attrValue = attrs.getValue(ac);

			if (attrUri == null || attrUri.length() == 0) {
				attrUri = uri.toString();
			}

			if (attrUri.equals(XMLConstants.XML_NS_URI) && "id".equals(attrLocalName)) {
				try {
					setId(new URI(null, null, attrValue));
				} catch (URISyntaxException e) {
					throw new IllegalArgumentException("Invalid xml:id value (no URI fragment)", e);
				}
				continue;
			}

			attributes.add(new XmlAttribute(URI.create(attrUri), attrPrefix, attrLocalName, attrValue));
		}
	}

	@Override
	public XPathAddress getXmlNodeAddress() {
		return xPathAddress;
	}

	@Override
	public Set<XmlAttribute> getAttributes() {
		return attributes;
	}
}