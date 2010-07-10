package org.lmnl.json;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.lmnl.LmnlAnnotation;

public class LmnlAnnotationSerializer extends JsonSerializer<LmnlAnnotation> {

	@Override
	public void serialize(LmnlAnnotation value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		doSerialize(value, jgen, provider);
		jgen.writeEndObject();

	}

	public static void doSerialize(LmnlAnnotation value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		LmnlLayerSerializer.doSerialize(value, jgen, provider);
		jgen.writeArrayFieldStart("range");
		jgen.writeNumber(value.address().start);
		jgen.writeNumber(value.address().end);
		jgen.writeEndArray();
	}
}
