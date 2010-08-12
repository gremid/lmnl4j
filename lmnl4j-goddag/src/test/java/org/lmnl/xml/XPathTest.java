package org.lmnl.xml;

import org.junit.Assert;
import org.junit.Test;
import org.lmnl.xml.tei.TeiUtil;

public class XPathTest extends XmlTest {

	@Test
	public void basicElementXPath() throws Exception {
		Assert.assertNotSame(0, TeiUtil.newXPathContext(documents.get(1)).selectNodes("/tei:TEI/tei:text").size());
	}

	@Test
	public void basicAttributeTest() throws Exception {
		Assert.assertNotSame(0, TeiUtil.newXPathContext(documents.get(1)).selectNodes("//tei:lg/attribute::*").size());
	}

	@Test
	public void followingAxis() throws Exception {
		Assert.assertNotNull(TeiUtil.newXPathContext(documents.get(1)).selectSingleNode("//tei:lb/following::text()"));

	}
}
