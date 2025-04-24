package eu.nerdfactor.restness.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.squareup.javapoet.ClassName;

import java.io.IOException;

/**
 * Serializer for {@link ClassName}s.
 *
 * @author Daniel Klug
 */
public class ClassNameSerializer extends StdSerializer<ClassName> {

	public ClassNameSerializer() {
		this(null);
	}

	public ClassNameSerializer(Class<ClassName> t) {
		super(t);
	}

	/**
	 * Serialize a {@link ClassName} and write it to a Json Node. It will take
	 * the full namespace and name of the class and write it as text.
	 *
	 * @param className          The {@link ClassName} to serialize.
	 * @param jsonGenerator      Generator used to output resulting Json
	 *                           content
	 * @param serializerProvider Provider that can be used to get serializers
	 *                           for serializing Objects value contains, if
	 *                           any.
	 */
	@Override
	public void serialize(ClassName className, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		jsonGenerator.writeString(className.box().toString());
	}
}
