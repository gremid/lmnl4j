package org.lmnl.xml;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.Assert;
import org.junit.Test;

public class XPathTest extends XmlTest {

	@Test
	public void basicElementXPath() throws Exception {
		Assert.assertNotSame(0, newXPath(documents.get(1)).selectNodes("/tei:TEI/tei:text").size());
	}

	@Test
	public void basicAttributeTest() throws Exception {
		Assert.assertNotSame(0, newXPath(documents.get(1)).selectNodes("//tei:lg/attribute::*").size());
	}
	
	private static JXPathContext newXPath(XmlAnnotationNode context) {
		JXPathContext xpath = context.newXPathContext();
		xpath.registerNamespace("tei", "http://www.tei-c.org/ns/1.0");
		return xpath;
	}
}
