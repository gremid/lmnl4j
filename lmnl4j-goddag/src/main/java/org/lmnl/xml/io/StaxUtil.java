package org.lmnl.xml.io;

import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.lmnl.AnnotationNode;
import org.lmnl.xml.Attribute;
import org.lmnl.xml.Comment;
import org.lmnl.xml.Document;
import org.lmnl.xml.Element;
import org.lmnl.xml.ProcessingInstruction;
import org.lmnl.xml.Text;
import org.lmnl.xml.XmlAnnotationNode;
import org.lmnl.xml.XmlAnnotationNodeFactory;
import org.lmnl.xml.XmlAnnotationNodeFilter;

public class StaxUtil {

    public static void exportToStream(XmlAnnotationNode node, XMLStreamWriter out) throws XMLStreamException {
        if (node instanceof Text) {
            out.writeCharacters(((Text) node).getContent());
        } else if (node instanceof Element) {
            Element element = (Element) node;
            boolean hasChildren = element.iterator().hasNext();
            String elementNs = element.getNamespace();
            if ("".equals(elementNs)) {
                if (hasChildren) {
                    out.writeStartElement(element.getName());
                } else {
                    out.writeEmptyElement(element.getName());
                }
            } else if (XMLConstants.XML_NS_URI.equals(elementNs)) {
                if (hasChildren) {
                    out.writeStartElement(XMLConstants.XML_NS_PREFIX, element.getName(), elementNs);
                } else {
                    out.writeEmptyElement(XMLConstants.XML_NS_PREFIX, element.getName(), elementNs);
                }
            } else {
                if (hasChildren) {
                    out.writeStartElement(elementNs, element.getName());
                } else {
                    out.writeEmptyElement(elementNs, element.getName());
                }
            }
            for (Attribute attribute : element.getAttributes()) {
                String attributeNs = attribute.getNamespace();
                if ("".equals(attributeNs)) {
                    out.writeAttribute(attribute.getName(), attribute.getValue());
                } else if (XMLConstants.XML_NS_URI.equals(attributeNs)) {
                    out.writeAttribute(XMLConstants.XML_NS_PREFIX, attributeNs, attribute.getName(), attribute.getValue());
                } else {
                    out.writeAttribute(attributeNs, attribute.getName(), attribute.getValue());
                }

            }
            if (hasChildren) {
                exportToStream(element.getChildNodes(), out);
                out.writeEndElement();
            }
        } else if (node instanceof Comment) {
            out.writeComment(((Comment) node).getComment());
        } else if (node instanceof ProcessingInstruction) {
            ProcessingInstruction pi = (ProcessingInstruction) node;
            String piData = pi.getData();
            if (piData == null) {
                out.writeProcessingInstruction(pi.getTarget());
            } else {
                out.writeProcessingInstruction(pi.getTarget(), piData);
            }
        } else if (node instanceof Document) {
            out.writeStartDocument();
            exportToStream(node.getChildNodes(), out);
            out.writeEndDocument();
        } else {
            exportToStream(node.getChildNodes(), out);
        }
    }

    public static void exportToStream(Iterable<AnnotationNode> nodes, XMLStreamWriter out) throws XMLStreamException {
        for (XmlAnnotationNode child : XmlAnnotationNodeFilter.filter(nodes)) {
            exportToStream(child, out);
        }
    }

    public static void importFromStream(XMLStreamReader in, XmlAnnotationNode to) throws XMLStreamException {
        final XmlAnnotationNodeFactory factory = (XmlAnnotationNodeFactory) to.getNodeFactory();

        StringBuilder characterBuf = null;
        Stack<XmlAnnotationNode> parents = new Stack<XmlAnnotationNode>();
        parents.push(to);

        for (; in.hasNext(); in.next()) {
            switch (in.getEventType()) {
            case XMLStreamConstants.START_ELEMENT:
                if (characterBuf != null) {
                    Text text = factory.createNode(Text.class, to.getRoot());
                    text.setContent(characterBuf.toString());

                    parents.peek().add(text);
                    characterBuf = null;
                }
                String ns = defaultNamespace(in.getNamespaceURI(), "");
                Element element = factory.createNode(Element.class, to.getRoot());
                element.setNamespace(ns);
                element.setName(in.getLocalName());
                for (int ac = 0; ac < in.getAttributeCount(); ac++) {
                    Attribute attr = factory.createNode(Attribute.class, to.getRoot());
                    attr.setNamespace(defaultNamespace(in.getAttributeNamespace(ac), ns));
                    attr.setName(in.getAttributeLocalName(ac));
                    attr.setValue(in.getAttributeValue(ac));
                    element.addAttribute(attr);
                }
                parents.peek().add(element);
                parents.push(element);
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (characterBuf != null) {
                    Text text = factory.createNode(Text.class, to.getRoot());
                    text.setContent(characterBuf.toString());

                    parents.peek().add(text);
                    characterBuf = null;
                }
                parents.pop();
                break;
            case XMLStreamConstants.CDATA:
            case XMLStreamConstants.ENTITY_REFERENCE:
            case XMLStreamConstants.CHARACTERS:
                if (characterBuf == null) {
                    characterBuf = new StringBuilder();
                }
                characterBuf.append(in.getText());
                break;
            case XMLStreamConstants.COMMENT:
                Comment comment = factory.createNode(Comment.class, to.getRoot());
                comment.setComment(in.getText());

                parents.peek().add(comment);
                break;
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                ProcessingInstruction pi = factory.createNode(ProcessingInstruction.class, to.getRoot());
                pi.setTarget(in.getPITarget());
                pi.setData(in.getPIData());

                parents.peek().add(pi);
                break;
            }
        }
    }

    private static String defaultNamespace(String ns, String defaultNs) {
        return (ns == null ? defaultNs : ns);
    }

}
