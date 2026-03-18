package eu.nerdfactor.restness.code;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.injector.OpenApiAnnotationInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;

@ExtendWith(MockitoExtension.class)
class OpenApiAnnotationInjectorTest {

	@Test
	void shouldNotAddAnnotationsWhenDisabled() {
		MethodSpec.Builder method = MethodSpec.methodBuilder("create")
				.addModifiers(Modifier.PUBLIC);

		new OpenApiAnnotationInjector()
				.withEnabled(false)
				.withOperationSummary("Create a new Example")
				.withOperationId("createExample")
				.withResponseCode("200")
				.withResponseDescription("Example created successfully")
				.inject(method);

		TypeSpec.Builder builder = TypeSpec.classBuilder("TestController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC)
				.addMethod(method.build());

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		Assertions.assertFalse(code.contains("@Operation"));
		Assertions.assertFalse(code.contains("@ApiResponse"));
	}

	@Test
	void shouldAddOperationAnnotation() {
		MethodSpec.Builder method = MethodSpec.methodBuilder("create")
				.addModifiers(Modifier.PUBLIC);

		new OpenApiAnnotationInjector()
				.withEnabled(true)
				.withOperationSummary("Create a new Example")
				.withOperationId("createExample")
				.withResponseCode("200")
				.withResponseDescription("Example created successfully")
				.inject(method);

		TypeSpec.Builder builder = TypeSpec.classBuilder("TestController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC)
				.addMethod(method.build());

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		Assertions.assertTrue(code.contains("@Operation("));
		Assertions.assertTrue(code.contains("summary = \"Create a new Example\""));
		Assertions.assertTrue(code.contains("operationId = \"createExample\""));
	}

	@Test
	void shouldAddApiResponsesAnnotation() {
		MethodSpec.Builder method = MethodSpec.methodBuilder("create")
				.addModifiers(Modifier.PUBLIC);

		new OpenApiAnnotationInjector()
				.withEnabled(true)
				.withOperationSummary("Create a new Example")
				.withOperationId("createExample")
				.withResponseCode("200")
				.withResponseDescription("Example created successfully")
				.addErrorResponse("400", "Invalid request body")
				.inject(method);

		TypeSpec.Builder builder = TypeSpec.classBuilder("TestController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC)
				.addMethod(method.build());

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		Assertions.assertTrue(code.contains("@ApiResponses("));
		Assertions.assertTrue(code.contains("responseCode = \"200\""));
		Assertions.assertTrue(code.contains("description = \"Example created successfully\""));
		Assertions.assertTrue(code.contains("responseCode = \"400\""));
		Assertions.assertTrue(code.contains("description = \"Invalid request body\""));
	}

	@Test
	void shouldAddMultipleErrorResponses() {
		MethodSpec.Builder method = MethodSpec.methodBuilder("update")
				.addModifiers(Modifier.PUBLIC);

		new OpenApiAnnotationInjector()
				.withEnabled(true)
				.withOperationSummary("Update Example")
				.withOperationId("updateExample")
				.withResponseCode("200")
				.withResponseDescription("Updated successfully")
				.addErrorResponse("400", "Invalid request body")
				.addErrorResponse("404", "Not found")
				.inject(method);

		TypeSpec.Builder builder = TypeSpec.classBuilder("TestController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC)
				.addMethod(method.build());

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		Assertions.assertTrue(code.contains("responseCode = \"400\""));
		Assertions.assertTrue(code.contains("responseCode = \"404\""));
		Assertions.assertTrue(code.contains("description = \"Not found\""));
	}
}
