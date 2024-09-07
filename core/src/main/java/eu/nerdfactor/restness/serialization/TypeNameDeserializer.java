package eu.nerdfactor.restness.serialization;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.io.IOException;

public class TypeNameDeserializer extends StdDeserializer<TypeName> {

	public TypeNameDeserializer() {
		this(null);
	}

	public TypeNameDeserializer(Class<TypeName> t) {
		super(t);
	}

	@Override
	public TypeName deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
		JsonNode node = jsonParser.readValueAsTree();
		String name = node.asText();
		return ClassName.bestGuess(name);
	}
}
