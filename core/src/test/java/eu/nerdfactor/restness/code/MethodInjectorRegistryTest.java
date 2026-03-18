package eu.nerdfactor.restness.code;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import eu.nerdfactor.restness.code.injector.ContextualInjectable;
import eu.nerdfactor.restness.code.injector.MethodContext;
import eu.nerdfactor.restness.code.injector.MethodInjectorRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MethodInjectorRegistryTest {

	@Test
	void shouldCreateEmptyRegistryWhenNoSpiProviders() {
		MethodInjectorRegistry registry = new MethodInjectorRegistry();
		assertFalse(registry.hasInjectors());
	}

	@Test
	void shouldReturnUnmodifiedMethodWhenNoInjectors() {
		MethodInjectorRegistry registry = new MethodInjectorRegistry();

		MethodSpec.Builder method = MethodSpec.methodBuilder("testMethod");
		MethodContext context = MethodContext.builder()
				.withMethodName("create")
				.withHttpMethod("POST")
				.withEntityType(ClassName.get("com.example", "Product"))
				.build();

		MethodSpec.Builder result = registry.injectAll(method, context);
		assertSame(method, result);
	}

	@Test
	void shouldApplyMatchingInjector() {
		MethodInjectorRegistry registry = new MethodInjectorRegistry(
				List.of(new TestAnnotationInjector("POST")));

		MethodSpec.Builder method = MethodSpec.methodBuilder("testMethod");
		MethodContext context = MethodContext.builder()
				.withMethodName("create")
				.withHttpMethod("POST")
				.withEntityType(ClassName.get("com.example", "Product"))
				.build();

		registry.injectAll(method, context);
		String code = method.build().toString();
		assertTrue(code.contains("TestAnnotation"));
	}

	@Test
	void shouldSkipNonMatchingInjector() {
		MethodInjectorRegistry registry = new MethodInjectorRegistry(
				List.of(new TestAnnotationInjector("DELETE")));

		MethodSpec.Builder method = MethodSpec.methodBuilder("testMethod");
		MethodContext context = MethodContext.builder()
				.withMethodName("create")
				.withHttpMethod("POST")
				.withEntityType(ClassName.get("com.example", "Product"))
				.build();

		registry.injectAll(method, context);
		String code = method.build().toString();
		assertFalse(code.contains("TestAnnotation"));
	}

	@Test
	void shouldApplyInjectorsInOrderByGetOrder() {
		MethodInjectorRegistry registry = new MethodInjectorRegistry(
				List.of(new OrderedInjector(2, "Second"), new OrderedInjector(1, "First")));

		MethodSpec.Builder method = MethodSpec.methodBuilder("testMethod");
		MethodContext context = MethodContext.builder()
				.withMethodName("test")
				.withHttpMethod("GET")
				.build();

		registry.injectAll(method, context);
		String code = method.build().toString();
		int firstIndex = code.indexOf("First");
		int secondIndex = code.indexOf("Second");
		assertTrue(firstIndex >= 0, "First annotation should be present");
		assertTrue(secondIndex >= 0, "Second annotation should be present");
		assertTrue(firstIndex < secondIndex, "First (order=1) should appear before Second (order=2)");
	}

	@Test
	void shouldReportHasInjectorsWhenPopulated() {
		MethodInjectorRegistry registry = new MethodInjectorRegistry(
				List.of(new TestAnnotationInjector("GET")));
		assertTrue(registry.hasInjectors());
	}

	/**
	 * Test injector that adds a @TestAnnotation when the HTTP method matches.
	 */
	private static class TestAnnotationInjector implements ContextualInjectable {
		private final String targetHttpMethod;

		TestAnnotationInjector(String targetHttpMethod) {
			this.targetHttpMethod = targetHttpMethod;
		}

		@Override
		public boolean appliesTo(MethodContext context) {
			return targetHttpMethod.equals(context.getHttpMethod());
		}

		@Override
		public MethodSpec.Builder inject(MethodSpec.Builder builder) {
			builder.addAnnotation(AnnotationSpec.builder(
					ClassName.get("eu.nerdfactor.test", "TestAnnotation")).build());
			return builder;
		}
	}

	/**
	 * Test injector with configurable order that adds a named annotation.
	 */
	private static class OrderedInjector implements ContextualInjectable {
		private final int order;
		private final String name;

		OrderedInjector(int order, String name) {
			this.order = order;
			this.name = name;
		}

		@Override
		public boolean appliesTo(MethodContext context) {
			return true;
		}

		@Override
		public MethodSpec.Builder inject(MethodSpec.Builder builder) {
			builder.addAnnotation(AnnotationSpec.builder(
					ClassName.get("eu.nerdfactor.test", name)).build());
			return builder;
		}

		@Override
		public int getOrder() {
			return order;
		}
	}
}
