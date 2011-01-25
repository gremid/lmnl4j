package org.lmnl.json;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.lmnl.Document;

public class DocumentSerializer extends JsonSerializer<Document> {

	@Override
	public void serialize(Document value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeArrayFieldStart("ns");
		for (String prefix : value.getNamespaceContext().keySet()) {
			jgen.writeStartObject();
			jgen.writeStringField("prefix", prefix);
			jgen.writeStringField("uri", value.getNamespaceContext().get(prefix).toString());
			jgen.writeEndObject();
		}
		jgen.writeEndArray();
		LayerSerializer.doSerialize(value, jgen, provider);
		jgen.writeEndObject();
	}
}
