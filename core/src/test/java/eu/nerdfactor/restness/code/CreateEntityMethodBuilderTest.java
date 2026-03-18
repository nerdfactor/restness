package eu.nerdfactor.restness.code;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.methodbuilder.CreateEntityMethodBuilder;
import eu.nerdfactor.restness.entity.Example;
import eu.nerdfactor.restness.entity.ExampleDto;
import eu.nerdfactor.restness.entity.ExampleForm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;

@ExtendWith(MockitoExtension.class)
class CreateEntityMethodBuilderTest {

	@Test
	void shouldCreateBasicMethod() {
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
		String expected = """
				@RestController
				public class ExampleController {
				  @PostMapping("/api/example")
				  @ResponseStatus(HttpStatus.CREATED)
				  public ResponseEntity<Example> create(@RequestBody @Valid Example dto) {
				    Example created = dto;
				    created = this.dataAccessor.createData(created);
				    Example response = created;
				    return new ResponseEntity<>(response, HttpStatus.CREATED);
				  }
				}
				""";
		Assertions.assertTrue(code.contains(expected));
	}

	@Test
	void shouldCreateMethodUsingDto() {
		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		CreateEntityMethodBuilder.create()
				.withRequestExists(false)
				.withUsingDto(true)
				.withBasePath("/api/example")
				.withEntityType(ClassName.get(Example.class))
				.withRequestBodyType(ClassName.get(ExampleForm.class))
				.withResponseBodyType(ClassName.get(ExampleDto.class))
				.withSecurityConfig(null)
				.withResponseWrapperType(TypeName.OBJECT)
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		String expected = """
				@RestController
				public class ExampleController {
				  @PostMapping("/api/example")
				  @ResponseStatus(HttpStatus.CREATED)
				  public ResponseEntity<ExampleDto> create(@RequestBody @Valid ExampleForm dto) {
				    Example created = this.dataMapper.map(dto, Example.class);
				    created = this.dataAccessor.createData(created);
				    ExampleDto response = this.dataMapper.map(created, ExampleDto.class);
				    return new ResponseEntity<>(response, HttpStatus.CREATED);
				  }
				}
				""";
		Assertions.assertTrue(code.contains(expected));
	}

	@Test
	void shouldCreateMethodWithOpenApiAnnotations() {
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
				.withOpenApi(true)
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		Assertions.assertTrue(code.contains("@Operation("));
		Assertions.assertTrue(code.contains("summary = \"Create a new Example\""));
		Assertions.assertTrue(code.contains("operationId = \"createExample\""));
		Assertions.assertTrue(code.contains("responseCode = \"201\""));
		Assertions.assertTrue(code.contains("responseCode = \"400\""));
	}

	@Test
	void shouldCreateMethodWithoutOpenApiAnnotations() {
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
				.withOpenApi(false)
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		Assertions.assertFalse(code.contains("@Operation("));
		Assertions.assertFalse(code.contains("@ApiResponse"));
	}
}
