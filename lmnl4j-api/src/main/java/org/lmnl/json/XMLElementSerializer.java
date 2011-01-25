package org.lmnl.json;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.lmnl.xml.XPath;
import org.lmnl.xml.XMLAttribute;
import org.lmnl.xml.XMLElement;

public class XMLElementSerializer extends JsonSerializer<XMLElement> {

	@Override
	public void serialize(XMLElement value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
			JsonProcessingException {
		jgen.writeStartObject();
		AnnotationSerializer.doSerialize(value, jgen, provider);

		final XPath xpath = value.getXPathAddress();
		if (xpath != null) {
			jgen.writeArrayFieldStart("xmlNode");
			for (int pos : xpath.get()) {
				jgen.writeNumber(pos);
			}
			jgen.writeEndArray();
		}
		if (!value.getAttributes().isEmpty()) {
			jgen.writeArrayFieldStart("attributes");
			for (XMLAttribute attr : value.getAttributes()) {
				jgen.writeStartObject();
				jgen.writeStringField("prefix", attr.prefix);
				jgen.writeStringField("localName", attr.localName);
				jgen.writeStringField("value", attr.value);
				jgen.writeEndObject();
			}
			jgen.writeEndArray();
		}

		jgen.writeEndObject();

	}

}
