package eu.nerdfactor.restness.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Deserializer for {@link ParameterizedTypeName}s. Supports nested generic
 * types like {@code Map<String, List<String>>}.
 *
 * @author Daniel Klug
 */
public class ParameterizedTypeNameDeserializer extends StdDeserializer<ParameterizedTypeName> {

	public ParameterizedTypeNameDeserializer() {
		this(null);
	}

	public ParameterizedTypeNameDeserializer(Class<ParameterizedTypeName> t) {
		super(t);
	}

	/**
	 * Deserializes a {@link ParameterizedTypeName} from a Json Node. It assumes
	 * that the Node contains the full class name (i.e. namespace and name) as
	 * text. Supports nested generic types.
	 *
	 * @param jsonParser             The {@link JsonParser} used to read from.
	 * @param deserializationContext The context of the deserialization.
	 * @return The deserialized {@link ParameterizedTypeName}.
	 */
	@Override
	public ParameterizedTypeName deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
		JsonNode node = jsonParser.readValueAsTree();
		String name = node.asText();
		TypeName parsed = parseTypeName(name);
		if (parsed instanceof ParameterizedTypeName parameterized) {
			return parameterized;
		}
		throw new IOException("Expected a parameterized type but got: " + name);
	}

	/**
	 * Parses a type name string into a {@link TypeName}, handling nested
	 * generic types recursively.
	 * <p>
	 * Examples:
	 * <li>{@code java.lang.String} → ClassName</li>
	 * <li>{@code java.util.List<java.lang.String>} → ParameterizedTypeName</li>
	 * <li>{@code java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>} → nested ParameterizedTypeName</li>
	 *
	 * @param typeString The string representation of the type.
	 * @return The parsed {@link TypeName}.
	 */
	public static TypeName parseTypeName(String typeString) {
		typeString = typeString.trim();
		int angleBracketIndex = typeString.indexOf('<');
		if (angleBracketIndex < 0) {
			return ClassName.bestGuess(typeString);
		}
		String rawType = typeString.substring(0, angleBracketIndex).trim();
		// Extract the content between the outermost < and >
		String argumentsStr = typeString.substring(angleBracketIndex + 1, typeString.lastIndexOf('>')).trim();
		List<String> arguments = splitTopLevelArguments(argumentsStr);
		TypeName[] typeArguments = arguments.stream()
				.map(ParameterizedTypeNameDeserializer::parseTypeName)
				.toArray(TypeName[]::new);
		return ParameterizedTypeName.get(ClassName.bestGuess(rawType), typeArguments);
	}

	/**
	 * Splits a comma-separated string of type arguments, respecting nested
	 * angle brackets. Only splits on commas at the top level (depth 0).
	 *
	 * @param arguments The type arguments string (without outer brackets).
	 * @return A list of individual type argument strings.
	 */
	private static List<String> splitTopLevelArguments(String arguments) {
		List<String> result = new ArrayList<>();
		int depth = 0;
		int start = 0;
		for (int i = 0; i < arguments.length(); i++) {
			char c = arguments.charAt(i);
			if (c == '<') {
				depth++;
			} else if (c == '>') {
				depth--;
			} else if (c == ',' && depth == 0) {
				result.add(arguments.substring(start, i).trim());
				start = i + 1;
			}
		}
		result.add(arguments.substring(start).trim());
		return result;
	}
}
