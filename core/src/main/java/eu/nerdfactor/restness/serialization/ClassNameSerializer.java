package eu.nerdfactor.restness.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.squareup.javapoet.ClassName;

import java.io.IOException;

public class ClassNameSerializer extends StdSerializer<ClassName> {

	public ClassNameSerializer() {
		this(null);
	}

	public ClassNameSerializer(Class<ClassName> t) {
		super(t);
	}

	@Override
	public void serialize(ClassName className, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		jsonGenerator.writeString(className.box().toString());
	}
}
