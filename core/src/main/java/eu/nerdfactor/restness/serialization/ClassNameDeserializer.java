package eu.nerdfactor.restness.serialization;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.squareup.javapoet.ClassName;

import java.io.IOException;

public class ClassNameDeserializer extends StdDeserializer<ClassName> {

	public ClassNameDeserializer() {
		this(null);
	}

	@Override
	public ClassName deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
		JsonNode node = jsonParser.readValueAsTree();
		String name = node.asText();
		return ClassName.bestGuess(name);
	}

	public ClassNameDeserializer(Class<ClassName> t) {
		super(t);
	}
}
