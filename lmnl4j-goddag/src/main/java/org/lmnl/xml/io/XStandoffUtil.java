package org.lmnl.xml.io;

import org.lmnl.xml.XmlAnnotationNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XStandoffUtil {
    private static final String XSTANDOFF_NS_URI = "http://www.xstandoff.net/2009/xstandoff/1.1";
    private static final String XSTANDOFF_SCHEMA_URI = "http://www.xstandoff.net/2009/xstandoff/1.1/xsf.xsd";
    private static final String XSTANDOFF_VERSION = "1.0";

    public static void exportToDom(Iterable<XmlAnnotationNode> levels, Node destination) {
        org.w3c.dom.Document document = (Document) (destination.getNodeType() == Node.DOCUMENT_NODE ? destination : destination
                .getOwnerDocument());
        
        for (XmlAnnotationNode level : levels) {
            Element levelElement = document.createElementNS(XSTANDOFF_NS_URI, "xsf:level");
            
        }
    }
}
