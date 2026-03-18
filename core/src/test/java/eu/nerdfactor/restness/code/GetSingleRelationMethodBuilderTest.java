package eu.nerdfactor.restness.code;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.methodbuilder.GetSingleRelationMethodBuilder;
import eu.nerdfactor.restness.entity.Example;
import eu.nerdfactor.restness.entity.ExampleDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;

@ExtendWith(MockitoExtension.class)
class GetSingleRelationMethodBuilderTest {

	@Test
	void shouldAddOpenApiAnnotationsWhenEnabled() {
		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		GetSingleRelationMethodBuilder.create()
				.withRequestMappings(new ArrayList<>())
				.withBasePath("/api/examples")
				.withEntityType(ClassName.get(Example.class))
				.withIdType(ClassName.get(Integer.class))
				.withSecurityConfig(null)
				.withResponseWrapperType(TypeName.OBJECT)
				.withRelationName("details")
				.withUsingDto(false)
				.withRelationResponseType(null)
				.withRelationEntityType(ClassName.get(ExampleDto.class))
				.withRelationGetter("getDetails")
				.withOpenApi(true)
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		Assertions.assertTrue(code.contains("@Operation("));
		Assertions.assertTrue(code.contains("summary = \"Get Details of Example\""));
		Assertions.assertTrue(code.contains("@ApiResponses("));
	}

	@Test
	void shouldNotAddOpenApiAnnotationsWhenDisabled() {
		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		GetSingleRelationMethodBuilder.create()
				.withRequestMappings(new ArrayList<>())
				.withBasePath("/api/examples")
				.withEntityType(ClassName.get(Example.class))
				.withIdType(ClassName.get(Integer.class))
				.withSecurityConfig(null)
				.withResponseWrapperType(TypeName.OBJECT)
				.withRelationName("details")
				.withUsingDto(false)
				.withRelationResponseType(null)
				.withRelationEntityType(ClassName.get(ExampleDto.class))
				.withRelationGetter("getDetails")
				.withOpenApi(false)
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		Assertions.assertFalse(code.contains("@Operation("));
		Assertions.assertFalse(code.contains("@ApiResponse"));
	}
}
