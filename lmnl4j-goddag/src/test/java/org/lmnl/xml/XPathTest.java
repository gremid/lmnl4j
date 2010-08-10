package org.lmnl.xml;

import org.junit.Assert;
import org.junit.Test;

public class XPathTest extends XmlTest {

	@Test
	public void basicElementXPath() throws Exception {
		Document algabal = documents.get(1);
		XmlAnnotationNodeXPath xpath = algabal.xpath("/tei:TEI/tei:text");
		xpath.addNamespace("tei", "http://www.tei-c.org/ns/1.0");
		Assert.assertNotSame(0, algabal.selectAllNodes(xpath).size());		
	}
}
