package org.lmnl.json;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.lmnl.LmnlDocument;

public class LmnlDocumentSerializer extends JsonSerializer<LmnlDocument> {

	@Override
	public void serialize(LmnlDocument value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeArrayFieldStart("ns");
		for (String prefix : value.getNamespaceContext().keySet()) {
			jgen.writeStartObject();
			jgen.writeStringField("prefix", prefix);
			jgen.writeStringField("uri", value.getNamespaceContext().get(prefix).toString());
			jgen.writeEndObject();
		}
		jgen.writeEndArray();
		LmnlLayerSerializer.doSerialize(value, jgen, provider);
		jgen.writeEndObject();
	}
}
