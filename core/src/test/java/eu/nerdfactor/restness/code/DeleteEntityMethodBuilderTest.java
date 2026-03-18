package eu.nerdfactor.restness.code;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.methodbuilder.DeleteEntityMethodBuilder;
import eu.nerdfactor.restness.entity.Example;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;

@ExtendWith(MockitoExtension.class)
class DeleteEntityMethodBuilderTest {

	@Test
	void shouldCreateBasicMethod() {
		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		DeleteEntityMethodBuilder.create()
				.withRequestExists(false)
				.withRequestPath("/api/example")
				.withEntityType(ClassName.get(Example.class))
				.withIdType(ClassName.get(Integer.class))
				.withSecurityConfig(null)
				.withResponseWrapperType(TypeName.OBJECT)
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();
		String expected = """
				@RestController
				public class ExampleController {
				  @DeleteMapping("/api/example")
				  @ResponseStatus(HttpStatus.NO_CONTENT)
				  public ResponseEntity<?> delete(@PathVariable final Integer id) {
				    this.dataAccessor.deleteDataById(id);
				    return ResponseEntity.noContent().build();
				  }
				}
				""";
		Assertions.assertTrue(code.contains(expected));
	}
}
