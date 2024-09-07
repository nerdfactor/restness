package eu.nerdfactor.restness.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.io.IOException;

public class ParameterizedTypeNameSerializer extends StdSerializer<ParameterizedTypeName> {

	public ParameterizedTypeNameSerializer() {
		this(null);
	}

	public ParameterizedTypeNameSerializer(Class<ParameterizedTypeName> t) {
		super(t);
	}

	@Override
	public void serialize(ParameterizedTypeName typeName, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		jsonGenerator.writeString(typeName.box().toString());
	}
}
