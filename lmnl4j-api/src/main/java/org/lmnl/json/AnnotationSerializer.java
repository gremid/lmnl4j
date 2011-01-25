package org.lmnl.json;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.lmnl.Annotation;

public class AnnotationSerializer extends JsonSerializer<Annotation> {

	@Override
	public void serialize(Annotation value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		doSerialize(value, jgen, provider);
		jgen.writeEndObject();

	}

	public static void doSerialize(Annotation value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		LayerSerializer.doSerialize(value, jgen, provider);
		jgen.writeArrayFieldStart("range");
		jgen.writeNumber(value.address().start);
		jgen.writeNumber(value.address().end);
		jgen.writeEndArray();
	}
}
