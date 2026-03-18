package eu.nerdfactor.restness.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.restness.export.ConfigMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParameterizedTypeNameDeserializerTest {

	@Test
	void shouldParseSimpleParameterizedType() {
		String input = "java.util.List<java.lang.String>";
		TypeName result = ParameterizedTypeNameDeserializer.parseTypeName(input);

		assertInstanceOf(ParameterizedTypeName.class, result);
		ParameterizedTypeName parameterized = (ParameterizedTypeName) result;
		assertEquals(ClassName.get(List.class), parameterized.rawType);
		assertEquals(1, parameterized.typeArguments.size());
		assertEquals(ClassName.get(String.class), parameterized.typeArguments.get(0));
	}

	@Test
	void shouldParseTwoArgumentParameterizedType() {
		String input = "java.util.Map<java.lang.String, java.lang.Integer>";
		TypeName result = ParameterizedTypeNameDeserializer.parseTypeName(input);

		assertInstanceOf(ParameterizedTypeName.class, result);
		ParameterizedTypeName parameterized = (ParameterizedTypeName) result;
		assertEquals(ClassName.get(Map.class), parameterized.rawType);
		assertEquals(2, parameterized.typeArguments.size());
		assertEquals(ClassName.get(String.class), parameterized.typeArguments.get(0));
		assertEquals(ClassName.get(Integer.class), parameterized.typeArguments.get(1));
	}

	@Test
	void shouldParseNestedParameterizedType() {
		String input = "java.util.Map<java.lang.String, java.util.List<java.lang.String>>";
		TypeName result = ParameterizedTypeNameDeserializer.parseTypeName(input);

		assertInstanceOf(ParameterizedTypeName.class, result);
		ParameterizedTypeName parameterized = (ParameterizedTypeName) result;
		assertEquals(ClassName.get(Map.class), parameterized.rawType);
		assertEquals(2, parameterized.typeArguments.size());
		assertEquals(ClassName.get(String.class), parameterized.typeArguments.get(0));

		// Second argument should be List<String>
		assertInstanceOf(ParameterizedTypeName.class, parameterized.typeArguments.get(1));
		ParameterizedTypeName nested = (ParameterizedTypeName) parameterized.typeArguments.get(1);
		assertEquals(ClassName.get(List.class), nested.rawType);
		assertEquals(ClassName.get(String.class), nested.typeArguments.get(0));
	}

	@Test
	void shouldParseDeeplyNestedParameterizedType() {
		String input = "java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>>";
		TypeName result = ParameterizedTypeNameDeserializer.parseTypeName(input);

		assertInstanceOf(ParameterizedTypeName.class, result);
		ParameterizedTypeName outer = (ParameterizedTypeName) result;
		assertEquals(ClassName.get(Map.class), outer.rawType);

		// Second argument: Map<String, List<Integer>>
		assertInstanceOf(ParameterizedTypeName.class, outer.typeArguments.get(1));
		ParameterizedTypeName middle = (ParameterizedTypeName) outer.typeArguments.get(1);
		assertEquals(ClassName.get(Map.class), middle.rawType);

		// Inner: List<Integer>
		assertInstanceOf(ParameterizedTypeName.class, middle.typeArguments.get(1));
		ParameterizedTypeName inner = (ParameterizedTypeName) middle.typeArguments.get(1);
		assertEquals(ClassName.get(List.class), inner.rawType);
		assertEquals(ClassName.get(Integer.class), inner.typeArguments.get(0));
	}

	@Test
	void shouldParseSimpleClassName() {
		String input = "java.lang.String";
		TypeName result = ParameterizedTypeNameDeserializer.parseTypeName(input);

		assertInstanceOf(ClassName.class, result);
		assertEquals(ClassName.get(String.class), result);
	}

	@Test
	void shouldRoundTripSimpleParameterizedType() throws Exception {
		ParameterizedTypeName original = ParameterizedTypeName.get(
				ClassName.get(List.class), ClassName.get(String.class));

		ObjectMapper mapper = ConfigMapper.forJson();
		String json = mapper.writeValueAsString(original);
		ParameterizedTypeName deserialized = mapper.readValue(json, ParameterizedTypeName.class);

		assertEquals(original, deserialized);
	}

	@Test
	void shouldRoundTripNestedParameterizedType() throws Exception {
		ParameterizedTypeName inner = ParameterizedTypeName.get(
				ClassName.get(List.class), ClassName.get(String.class));
		ParameterizedTypeName original = ParameterizedTypeName.get(
				ClassName.get(Map.class), ClassName.get(String.class), inner);

		ObjectMapper mapper = ConfigMapper.forJson();
		String json = mapper.writeValueAsString(original);
		ParameterizedTypeName deserialized = mapper.readValue(json, ParameterizedTypeName.class);

		assertEquals(original, deserialized);
	}
}
