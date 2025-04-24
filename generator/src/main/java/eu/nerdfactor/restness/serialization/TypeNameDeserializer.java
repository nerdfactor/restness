package eu.nerdfactor.restness.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.io.IOException;

/**
 * Deserializer for {@link TypeName}s.
 *
 * @author Daniel Klug
 */
public class TypeNameDeserializer extends StdDeserializer<TypeName> {

	public TypeNameDeserializer() {
		this(null);
	}

	public TypeNameDeserializer(Class<TypeName> t) {
		super(t);
	}

	/**
	 * Deserializes a {@link TypeName} from a Json Node. It assumes that the
	 * Node contains the full clas name (i.e. namespace and name) as text.
	 *
	 * @param jsonParser             The {@link JsonParser} used to read from.
	 * @param deserializationContext The context of the deserialization.
	 * @return The deserialized {@link TypeName}.
	 */
	@Override
	public TypeName deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
		JsonNode node = jsonParser.readValueAsTree();
		String name = node.asText();
		return ClassName.bestGuess(name);
	}
}
