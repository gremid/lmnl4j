package org.lmnl.json;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.lmnl.xml.XmlAttribute;
import org.lmnl.xml.XmlElementAnnotation;

public class XmlElementAnnotationSerializer extends JsonSerializer<XmlElementAnnotation> {

	@Override
	public void serialize(XmlElementAnnotation value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		LmnlAnnotationSerializer.doSerialize(value, jgen, provider);

		jgen.writeArrayFieldStart("xmlNode");
		for (int pos : value.getXmlNodeAddress().get()) {
			jgen.writeNumber(pos);
		}
		jgen.writeEndArray();

		if (!value.getAttributes().isEmpty()) {
			jgen.writeArrayFieldStart("attributes");
			for (XmlAttribute attr : value.getAttributes()) {
				jgen.writeStartObject();
				jgen.writeStringField("ns", attr.ns.toASCIIString());
				jgen.writeStringField("localName", attr.localName);
				jgen.writeStringField("value", attr.value);
				jgen.writeEndObject();
			}
			jgen.writeEndArray();
		}

		jgen.writeEndObject();

	}

}