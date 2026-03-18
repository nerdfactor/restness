package eu.nerdfactor.restness.code;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.entity.Example;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;

@ExtendWith(MockitoExtension.class)
class RestnessControllerBuilderTest {

	@Test
	void shouldAddTagAnnotationWhenOpenApiEnabled() {
		ControllerConfiguration config = ControllerConfiguration.builder()
				.withControllerClassName(ClassName.get("eu.nerdfactor.test", "ExampleController"))
				.withRequestBasePath("/api/examples")
				.withEntityType(ClassName.get(Example.class))
				.withIdType(ClassName.get(Integer.class))
				.withRequestObjectClassName(TypeName.OBJECT)
				.withResponseObjectClassName(TypeName.OBJECT)
				.withExistingRequestMappings(new ArrayList<>())
				.withRelationConfigurations(new HashMap<>())
				.build();
		config.setOpenApi(true);

		TypeSpec typeSpec = RestnessControllerBuilder.create()
				.withConfiguration(config)
				.build();

		String code = JavaFile.builder("eu.nerdfactor.test", typeSpec).build().toString();
		Assertions.assertTrue(code.contains("@Tag("));
		Assertions.assertTrue(code.contains("name = \"Example\""));
		Assertions.assertTrue(code.contains("description = \"Example management endpoints\""));
	}

	@Test
	void shouldNotAddTagAnnotationWhenOpenApiDisabled() {
		ControllerConfiguration config = ControllerConfiguration.builder()
				.withControllerClassName(ClassName.get("eu.nerdfactor.test", "ExampleController"))
				.withRequestBasePath("/api/examples")
				.withEntityType(ClassName.get(Example.class))
				.withIdType(ClassName.get(Integer.class))
				.withRequestObjectClassName(TypeName.OBJECT)
				.withResponseObjectClassName(TypeName.OBJECT)
				.withExistingRequestMappings(new ArrayList<>())
				.withRelationConfigurations(new HashMap<>())
				.build();
		config.setOpenApi(false);

		TypeSpec typeSpec = RestnessControllerBuilder.create()
				.withConfiguration(config)
				.build();

		String code = JavaFile.builder("eu.nerdfactor.test", typeSpec).build().toString();
		Assertions.assertFalse(code.contains("@Tag("));
	}
}
