package org.lmnl.json;

import java.io.IOException;
import java.net.URI;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.lmnl.Annotation;
import org.lmnl.Layer;

import com.google.common.collect.Iterables;

public class LayerSerializer extends JsonSerializer<Layer> {

	@Override
	public void serialize(Layer value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		doSerialize(value, jgen, provider);
		jgen.writeEndObject();
	}

	public static void doSerialize(Layer value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
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
			for (Annotation annotation : value) {
				provider.findValueSerializer(annotation.getClass()).serialize(annotation, jgen, provider);
			}
			jgen.writeEndArray();
		}
	}
}
