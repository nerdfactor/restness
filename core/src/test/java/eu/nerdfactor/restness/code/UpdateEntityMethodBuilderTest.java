package eu.nerdfactor.restness.code;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.methodbuilder.UpdateEntityMethodBuilder;
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
class UpdateEntityMethodBuilderTest {

	@Test
	void shouldCreateBasicMethod() {
		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		UpdateEntityMethodBuilder.create()
				.withRequestExists(false)
				.withUsingDto(false)
				.withBasePath("/api/example")
				.withEntityType(ClassName.get(Example.class))
				.withRequestBodyType(ClassName.get(Example.class))
				.withResponseBodyType(ClassName.get(Example.class))
				.withIdType(ClassName.get(Integer.class))
				.withSecurityConfig(null)
				.withResponseWrapperType(TypeName.OBJECT)
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		String expected = """
				@RestController
				public class ExampleController {
				  @PatchMapping("/api/example")
				  @ResponseStatus(HttpStatus.OK)
				  public ResponseEntity<Example> update(@PathVariable final Integer id,
				      @RequestBody @Valid Example dto) {
				    Example entity = this.dataAccessor.readData(id).orElseThrow(EntityNotFoundException::new);
				    Example changed = dto;
				    Example updated = this.dataMerger.merge(entity, changed);
				    updated = this.dataAccessor.updateData(updated);
				    Example response = updated;
				    return new ResponseEntity<>(response, HttpStatus.OK);
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

		UpdateEntityMethodBuilder.create()
				.withRequestExists(false)
				.withUsingDto(true)
				.withBasePath("/api/example")
				.withEntityType(ClassName.get(Example.class))
				.withRequestBodyType(ClassName.get(ExampleForm.class))
				.withResponseBodyType(ClassName.get(ExampleDto.class))
				.withIdType(ClassName.get(Integer.class))
				.withSecurityConfig(null)
				.withResponseWrapperType(TypeName.OBJECT)
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		String expected = """
				@RestController
				public class ExampleController {
				  @PatchMapping("/api/example")
				  @ResponseStatus(HttpStatus.OK)
				  public ResponseEntity<ExampleDto> update(@PathVariable final Integer id,
				      @RequestBody @Valid ExampleForm dto) {
				    Example entity = this.dataAccessor.readData(id).orElseThrow(EntityNotFoundException::new);
				    Example changed = this.dataMapper.map(dto, Example.class);
				    Example updated = this.dataMerger.merge(entity, changed);
				    updated = this.dataAccessor.updateData(updated);
				    ExampleDto response = this.dataMapper.map(updated, ExampleDto.class);
				    return new ResponseEntity<>(response, HttpStatus.OK);
				  }
				}
				""";
		Assertions.assertTrue(code.contains(expected));
	}
}
