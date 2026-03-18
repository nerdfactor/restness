package eu.nerdfactor.restness.code;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.injector.ContextualInjectable;
import eu.nerdfactor.restness.code.injector.MethodContext;
import eu.nerdfactor.restness.code.injector.MethodInjectorRegistry;
import eu.nerdfactor.restness.code.methodbuilder.CreateEntityMethodBuilder;
import eu.nerdfactor.restness.code.methodbuilder.DeleteEntityMethodBuilder;
import eu.nerdfactor.restness.entity.Example;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test verifying that custom {@link ContextualInjectable}
 * implementations are correctly wired through the builder hierarchy
 * and applied to generated methods.
 */
class ContextualInjectableIntegrationTest {

	@Test
	void shouldInjectCustomAnnotationIntoCreateMethod() {
		MethodInjectorRegistry registry = new MethodInjectorRegistry(
				List.of(new PostOnlyInjector()));

		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		CreateEntityMethodBuilder.create()
				.withRequestExists(false)
				.withUsingDto(false)
				.withBasePath("/api/example")
				.withEntityType(ClassName.get(Example.class))
				.withRequestBodyType(ClassName.get(Example.class))
				.withResponseBodyType(ClassName.get(Example.class))
				.withSecurityConfig(null)
				.withResponseWrapperType(TypeName.OBJECT)
				.withInjectorRegistry(registry)
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		assertTrue(code.contains("CustomRateLimit"), "Custom injector annotation should be present in POST method");
	}

	@Test
	void shouldNotInjectWhenContextDoesNotMatch() {
		MethodInjectorRegistry registry = new MethodInjectorRegistry(
				List.of(new PostOnlyInjector()));

		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		DeleteEntityMethodBuilder.create()
				.withRequestExists(false)
				.withRequestPath("/api/example/{id}")
				.withEntityType(ClassName.get(Example.class))
				.withIdType(ClassName.get(Integer.class))
				.withSecurityConfig(null)
				.withResponseWrapperType(TypeName.OBJECT)
				.withInjectorRegistry(registry)
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		assertFalse(code.contains("CustomRateLimit"), "Custom injector should NOT be applied to DELETE method");
	}

	@Test
	void shouldWorkWithNullRegistry() {
		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		CreateEntityMethodBuilder.create()
				.withRequestExists(false)
				.withUsingDto(false)
				.withBasePath("/api/example")
				.withEntityType(ClassName.get(Example.class))
				.withRequestBodyType(ClassName.get(Example.class))
				.withResponseBodyType(ClassName.get(Example.class))
				.withSecurityConfig(null)
				.withResponseWrapperType(TypeName.OBJECT)
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		assertTrue(code.contains("create"), "Method should be generated without custom injections");
		assertFalse(code.contains("CustomRateLimit"), "No custom annotations without registry");
	}

	/**
	 * A test injector that only applies to POST methods, adding a @CustomRateLimit annotation.
	 */
	private static class PostOnlyInjector implements ContextualInjectable {

		@Override
		public boolean appliesTo(MethodContext context) {
			return "POST".equals(context.getHttpMethod());
		}

		@Override
		public MethodSpec.Builder inject(MethodSpec.Builder builder) {
			builder.addAnnotation(AnnotationSpec.builder(
					ClassName.get("eu.nerdfactor.test", "CustomRateLimit")).build());
			return builder;
		}
	}
}
