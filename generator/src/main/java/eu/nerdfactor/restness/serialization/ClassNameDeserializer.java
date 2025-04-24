package eu.nerdfactor.restness.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.squareup.javapoet.ClassName;

import java.io.IOException;

/**
 * Deserializer for {@link ClassName}s.
 *
 * @author Daniel Klug
 */
public class ClassNameDeserializer extends StdDeserializer<ClassName> {

	public ClassNameDeserializer() {
		this(null);
	}

	public ClassNameDeserializer(Class<ClassName> t) {
		super(t);
	}

	/**
	 * Deserializes a {@link ClassName} from a Json Node. It assumes that the
	 * Node contains the full clas name (i.e. namespace and name) as text.
	 *
	 * @param jsonParser             The {@link JsonParser} used to read from.
	 * @param deserializationContext The context of the deserialization.
	 * @return The deserialized {@link ClassName}.
	 */
	@Override
	public ClassName deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
		JsonNode node = jsonParser.readValueAsTree();
		String name = node.asText();
		return ClassName.bestGuess(name);
	}
}
