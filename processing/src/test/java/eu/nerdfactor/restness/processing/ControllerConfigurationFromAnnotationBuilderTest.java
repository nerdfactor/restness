package eu.nerdfactor.restness.processing;

import com.squareup.javapoet.ClassName;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class ControllerConfigurationFromAnnotationBuilderTest {

	@Mock
	private Elements elementUtils;

	@Mock
	private RoundEnvironment environment;

	@Mock
	private TypeElement mockElement;

	@Mock
	private PackageElement packageElement;

	@Mock
	private Name qualifiedName;

	@Mock
	private Name entitySimpleName;

	@Mock
	private TypeElement mockEntityElement;

	@BeforeEach
	void setup() {
		// Setup mocks
		when(elementUtils.getPackageOf(any(TypeElement.class))).thenReturn(packageElement);
		when(packageElement.getQualifiedName()).thenReturn(qualifiedName);
		when(qualifiedName.toString()).thenReturn("eu.nerdfactor.test");
		when(mockElement.getSimpleName()).thenReturn(mock(Name.class));

		// Setup entity element name for lookup
		when(mockEntityElement.getSimpleName()).thenReturn(entitySimpleName);
		when(entitySimpleName.toString()).thenReturn("TestEntity");

		// Setup root elements with entity
		Set<Element> rootElements = new HashSet<>();
		rootElements.add(mockEntityElement);
		when(environment.getRootElements()).thenReturn((Set) rootElements);
	}

	@Test
	void shouldBuildBasicConfiguration() {
		// Arrange
		Map<String, String> annotatedValues = new HashMap<>();
		annotatedValues.put("value", "/api/test");
		annotatedValues.put("entity", "eu.nerdfactor.test.TestEntity");
		annotatedValues.put("id", "java.lang.Integer");
		annotatedValues.put("withRelations", "false");

		// Act
		ControllerConfiguration config = ControllerConfigurationFromAnnotationBuilder.create()
				.withEnvironment(environment)
				.withUtils(elementUtils)
				.withElement(mockElement)
				.withPrefix("Generated")
				.withPattern("{PREFIX}{NAME}")
				.withAnnotatedValues(annotatedValues)
				.build();

		// Assert
		assertNotNull(config);
		assertEquals("/api/test", config.getRequestBasePath());
		assertEquals(ClassName.bestGuess("eu.nerdfactor.test.TestEntity"), config.getEntityType());
		assertEquals(ClassName.bestGuess("java.lang.Integer"), config.getIdType());
		assertEquals("getId", config.getIdAccessorMethodName());
		assertEquals("setId", config.getIdModifierMethodName());
		assertFalse(config.isUsingDto());
		assertFalse(config.isUsingRelations());
	}

	@Test
	void shouldBuildConfigurationWithDto() {
		// Arrange
		Map<String, String> annotatedValues = new HashMap<>();
		annotatedValues.put("value", "/api/test");
		annotatedValues.put("entity", "eu.nerdfactor.test.TestEntity");
		annotatedValues.put("id", "java.lang.Integer");
		annotatedValues.put("dto", "eu.nerdfactor.test.TestDto");
		annotatedValues.put("withRelations", "false");

		// Act
		ControllerConfiguration config = ControllerConfigurationFromAnnotationBuilder.create()
				.withEnvironment(environment)
				.withUtils(elementUtils)
				.withElement(mockElement)
				.withPrefix("Generated")
				.withPattern("{PREFIX}{NAME}")
				.withAnnotatedValues(annotatedValues)
				.build();

		// Assert
		assertNotNull(config);
		assertTrue(config.isUsingDto());
		assertEquals(ClassName.bestGuess("eu.nerdfactor.test.TestDto"), config.getResponseBodyType());
	}

	@Test
	void shouldBuildWithCustomIdAccessors() {
		// Arrange
		Map<String, String> annotatedValues = new HashMap<>();
		annotatedValues.put("value", "/api/test");
		annotatedValues.put("entity", "eu.nerdfactor.test.TestEntity");
		annotatedValues.put("id", "java.lang.Integer");
		annotatedValues.put("withRelations", "false");

		// Act
		ControllerConfiguration config = ControllerConfigurationFromAnnotationBuilder.create()
				.withEnvironment(environment)
				.withUtils(elementUtils)
				.withElement(mockElement)
				.withPrefix("Generated")
				.withPattern("{PREFIX}{NAME}")
				.withAnnotatedValues(annotatedValues)
				.build();

		// Assert
		assertNotNull(config);
		assertEquals("getId", config.getIdAccessorMethodName());
		assertEquals("setId", config.getIdModifierMethodName());
	}
}
