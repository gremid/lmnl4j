package org.lmnl.xml;

import java.net.URI;

import org.lmnl.lom.LmnlAnnotation;

/**
 * Interface to factories used to create corresponding annotations for XML
 * elements and attributes.
 * 
 * <p/>
 * 
 * Depending on whether a factory creates annotations, that implement
 * {@link XmlNodeSourced}, the converter will optionally assign information of
 * relative node positions to the created annotations.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public interface XmlBasedLmnlAnnotationFactory {
	/**
	 * Creates an annotation corresponding to an XML element.
	 * 
	 * @param ns
	 *                the namespace URI of the XML element
	 * @param prefix
	 *                the prefix of the element's name
	 * @param localName
	 *                the element's local name
	 * @param startOffset
	 *                the offset of the range's start. Note that the end
	 *                offset will be set later on the range's address,
	 *                accessing it via {@link LmnlRange#address()}
	 * @return a range annotation representing the XML element
	 */
	LmnlAnnotation createElementRange(URI ns, String prefix, String localName, int startOffset);

	/**
	 * Creates an annotation corresponding to an XML attribute.
	 * 
	 * @param ns
	 *                the namespace URI of the XML attribute, defaulting to
	 *                the attribute's element namespace
	 * @param prefix
	 *                the attribute's namespace prefix
	 * @param localName
	 *                the local name of the attribute
	 * @param value
	 *                the attribute's value
	 * @return an annotation representing the XML attribute
	 */
	LmnlAnnotation createAttributeAnnotation(LmnlAnnotation elementAnnotation, URI ns, String prefix, String localName, String value);

	/**
	 * Hands over the text collected by the XML processor during model
	 * creation.
	 * 
	 * @param text
	 *                the combinded text nodes of the processed XML data
	 */
	void createText(String text);
}