package eu.nerdfactor.restness.code;

import com.squareup.javapoet.ClassName;
import eu.nerdfactor.restness.code.injector.MethodContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MethodContextTest {

	@Test
	void shouldBuildEntityMethodContext() {
		MethodContext context = MethodContext.builder()
				.withMethodName("create")
				.withHttpMethod("POST")
				.withEntityType(ClassName.get("com.example", "Product"))
				.build();

		assertEquals("create", context.getMethodName());
		assertEquals("POST", context.getHttpMethod());
		assertEquals(ClassName.get("com.example", "Product"), context.getEntityType());
		assertNull(context.getRelatedEntityType());
		assertNull(context.getRelationName());
	}

	@Test
	void shouldBuildRelationMethodContext() {
		MethodContext context = MethodContext.builder()
				.withMethodName("getProductOrders")
				.withHttpMethod("GET")
				.withEntityType(ClassName.get("com.example", "Product"))
				.withRelatedEntityType(ClassName.get("com.example", "Order"))
				.withRelationName("orders")
				.build();

		assertEquals("getProductOrders", context.getMethodName());
		assertEquals("GET", context.getHttpMethod());
		assertEquals(ClassName.get("com.example", "Product"), context.getEntityType());
		assertEquals(ClassName.get("com.example", "Order"), context.getRelatedEntityType());
		assertEquals("orders", context.getRelationName());
	}

	@Test
	void shouldAllowNullFieldsForMinimalContext() {
		MethodContext context = MethodContext.builder()
				.withMethodName("delete")
				.withHttpMethod("DELETE")
				.build();

		assertEquals("delete", context.getMethodName());
		assertEquals("DELETE", context.getHttpMethod());
		assertNull(context.getEntityType());
		assertNull(context.getRelatedEntityType());
		assertNull(context.getRelationName());
	}
}
