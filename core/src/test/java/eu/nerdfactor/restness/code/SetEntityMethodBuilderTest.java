package eu.nerdfactor.restness.code;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
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
public class SetEntityMethodBuilderTest {

	@Test
	void shouldCreateBasicMethod() {
		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		SetEntityMethodBuilder.create()
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
				  @PutMapping("/api/example")
				  public ResponseEntity<Example> set(@PathVariable final Integer id,
				      @RequestBody @Valid Example dto) {
				    Example entity = this.dataAccessor.readData(id);
				    if(entity == null) {
				      throw new EntityNotFoundException();
				    }
				    Example changed = dto;
				    changed = this.dataAccessor.updateData(changed);
				    Example response = changed;
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

		SetEntityMethodBuilder.create()
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
				  @PutMapping("/api/example")
				  public ResponseEntity<ExampleDto> set(@PathVariable final Integer id,
				      @RequestBody @Valid ExampleForm dto) {
				    Example entity = this.dataAccessor.readData(id);
				    if(entity == null) {
				      throw new EntityNotFoundException();
				    }
				    Example changed = this.dataMapper.map(dto, Example.class);
				    changed = this.dataAccessor.updateData(changed);
				    ExampleDto response = this.dataMapper.map(changed, ExampleDto.class);
				    return new ResponseEntity<>(response, HttpStatus.OK);
				  }
				}
				""";
		Assertions.assertTrue(code.contains(expected));
	}

}
