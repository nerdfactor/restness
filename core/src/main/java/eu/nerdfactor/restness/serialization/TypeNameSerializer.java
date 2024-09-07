package eu.nerdfactor.restness.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.io.IOException;

public class TypeNameSerializer extends StdSerializer<TypeName> {

	public TypeNameSerializer() {
		this(null);
	}

	public TypeNameSerializer(Class<TypeName> t) {
		super(t);
	}

	@Override
	public void serialize(TypeName typeName, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		jsonGenerator.writeString(typeName.box().toString());
	}
}
