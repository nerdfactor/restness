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
public class UpdateEntityMethodBuilderTest {

	@Test
	void shouldCreateBasicMethod() {
		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		UpdateEntityMethodBuilder.create()
				.withHasExistingRequest(false)
				.withUsingDto(false)
				.withRequestUrl("/api/example")
				.withEntityType(ClassName.get(Example.class))
				.withRequestType(ClassName.get(Example.class))
				.withResponseType(ClassName.get(Example.class))
				.withIdentifyingType(ClassName.get(Integer.class))
				.withSecurityConfiguration(null)
				.withDataWrapperClass(TypeName.OBJECT)
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		String expected = """
				@RestController
				public class ExampleController {
				  @PatchMapping("/api/example")
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
				.withHasExistingRequest(false)
				.withUsingDto(true)
				.withRequestUrl("/api/example")
				.withEntityType(ClassName.get(Example.class))
				.withRequestType(ClassName.get(ExampleForm.class))
				.withResponseType(ClassName.get(ExampleDto.class))
				.withIdentifyingType(ClassName.get(Integer.class))
				.withSecurityConfiguration(null)
				.withDataWrapperClass(TypeName.OBJECT)
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		String expected = """
				@RestController
				public class ExampleController {
				  @PatchMapping("/api/example")
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
