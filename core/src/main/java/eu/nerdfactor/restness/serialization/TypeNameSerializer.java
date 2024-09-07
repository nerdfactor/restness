package eu.nerdfactor.restness.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.squareup.javapoet.TypeName;

import java.io.IOException;

/**
 * Serializer for {@link TypeName}s.
 *
 * @author Daniel Klug
 */
public class TypeNameSerializer extends StdSerializer<TypeName> {

	public TypeNameSerializer() {
		this(null);
	}

	public TypeNameSerializer(Class<TypeName> t) {
		super(t);
	}

	/**
	 * Serialize a {@link TypeName} and write it to a Json Node. It will take
	 * the full namespace and name of the class and write it as text.
	 *
	 * @param typeName           The {@link TypeName} to serialize.
	 * @param jsonGenerator      Generator used to output resulting Json
	 *                           content
	 * @param serializerProvider Provider that can be used to get serializers
	 *                           for serializing Objects value contains, if
	 *                           any.
	 */
	@Override
	public void serialize(TypeName typeName, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		jsonGenerator.writeString(typeName.box().toString());
	}
}
