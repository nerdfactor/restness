package eu.nerdfactor.restness.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.squareup.javapoet.ParameterizedTypeName;

import java.io.IOException;

/**
 * Serializer for {@link ParameterizedTypeName}s.
 *
 * @author Daniel Klug
 */
public class ParameterizedTypeNameSerializer extends StdSerializer<ParameterizedTypeName> {

	public ParameterizedTypeNameSerializer() {
		this(null);
	}

	public ParameterizedTypeNameSerializer(Class<ParameterizedTypeName> t) {
		super(t);
	}

	/**
	 * Serialize a {@link ParameterizedTypeName} and write it to a Json Node. It
	 * will take the full namespace and name of the class and write it as text.
	 *
	 * @param typeName           The {@link ParameterizedTypeName} to
	 *                           serialize.
	 * @param jsonGenerator      Generator used to output resulting Json
	 *                           content
	 * @param serializerProvider Provider that can be used to get serializers
	 *                           for serializing Objects value contains, if
	 *                           any.
	 */
	@Override
	public void serialize(ParameterizedTypeName typeName, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		jsonGenerator.writeString(typeName.box().toString());
	}
}
