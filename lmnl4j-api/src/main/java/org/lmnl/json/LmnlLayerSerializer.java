package org.lmnl.json;

import java.io.IOException;
import java.net.URI;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.lmnl.LmnlAnnotation;
import org.lmnl.LmnlLayer;

import com.google.common.collect.Iterables;

public class LmnlLayerSerializer extends JsonSerializer<LmnlLayer> {

	@Override
	public void serialize(LmnlLayer value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		doSerialize(value, jgen, provider);
		jgen.writeEndObject();
	}

	public static void doSerialize(LmnlLayer value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		URI id = value.getId();		
		if (id != null) {
			jgen.writeStringField("id", id.toASCIIString());
		}

		jgen.writeStringField("name", value.getQName());
		if (value.hasText()) {
			jgen.writeStringField("text", value.getText());
		}
		if (!Iterables.isEmpty(value)) {
			jgen.writeArrayFieldStart("annotations");
			for (LmnlAnnotation annotation : value) {
				provider.findValueSerializer(annotation.getClass()).serialize(annotation, jgen, provider);
			}
			jgen.writeEndArray();
		}
	}
}
