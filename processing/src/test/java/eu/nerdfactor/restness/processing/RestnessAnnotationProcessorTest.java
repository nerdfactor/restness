package eu.nerdfactor.restness.processing;

import eu.nerdfactor.restness.annotation.RestnessConfiguration;
import eu.nerdfactor.restness.annotation.RestnessController;
import eu.nerdfactor.restness.annotation.RestnessSecurity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class RestnessAnnotationProcessorTest {

	@Mock
	private ProcessingEnvironment processingEnvironment;

	@Mock
	private Filer filer;

	@Mock
	private Elements elementUtils;

	@Mock
	private RoundEnvironment roundEnvironment;

	private RestnessAnnotationProcessor processor;

	@BeforeEach
	void setup() {
		when(processingEnvironment.getFiler()).thenReturn(filer);
		when(processingEnvironment.getElementUtils()).thenReturn(elementUtils);
		when(processingEnvironment.getSourceVersion()).thenReturn(SourceVersion.RELEASE_17);

		processor = new RestnessAnnotationProcessor();
		processor.init(processingEnvironment);
	}

	@Test
	void shouldInitializeSuccessfully() {
		assertNotNull(processor);
	}

	@Test
	void shouldReturnTrueOnEmptyRound() {
		Set<TypeElement> annotations = new HashSet<>();
		when(roundEnvironment.getElementsAnnotatedWith(RestnessConfiguration.class))
				.thenReturn((Set) Collections.emptySet());
		when(roundEnvironment.getElementsAnnotatedWith(RestnessController.List.class))
				.thenReturn((Set) Collections.emptySet());
		when(roundEnvironment.getElementsAnnotatedWith(RestnessController.class))
				.thenReturn((Set) Collections.emptySet());
		when(roundEnvironment.getElementsAnnotatedWith(RestnessSecurity.class))
				.thenReturn((Set) Collections.emptySet());

		boolean result = processor.process(annotations, roundEnvironment);

		assertTrue(result);
	}

	@Test
	void shouldSkipNonClassConfigurationAnnotation() {
		Set<TypeElement> annotations = new HashSet<>();

		// Create a mock element that is not a class (e.g., a method)
		Element nonClassElement = mock(Element.class);
		when(nonClassElement.getKind()).thenReturn(ElementKind.METHOD);

		Set<Element> configElements = new HashSet<>();
		configElements.add(nonClassElement);
		when(roundEnvironment.getElementsAnnotatedWith(RestnessConfiguration.class))
				.thenReturn((Set) configElements);

		boolean result = processor.process(annotations, roundEnvironment);

		assertTrue(result);
	}

	@Test
	void shouldSkipNonClassSecurityAnnotation() {
		Set<TypeElement> annotations = new HashSet<>();

		// Setup empty configuration and controller rounds
		when(roundEnvironment.getElementsAnnotatedWith(RestnessConfiguration.class))
				.thenReturn((Set) Collections.emptySet());
		when(roundEnvironment.getElementsAnnotatedWith(RestnessController.List.class))
				.thenReturn((Set) Collections.emptySet());
		when(roundEnvironment.getElementsAnnotatedWith(RestnessController.class))
				.thenReturn((Set) Collections.emptySet());

		// Create a mock security element that is not a class
		Element nonClassElement = mock(Element.class);
		when(nonClassElement.getKind()).thenReturn(ElementKind.FIELD);

		Set<Element> securityElements = new HashSet<>();
		securityElements.add(nonClassElement);
		when(roundEnvironment.getElementsAnnotatedWith(RestnessSecurity.class))
				.thenReturn((Set) securityElements);

		boolean result = processor.process(annotations, roundEnvironment);

		assertTrue(result);
	}

	@Test
	void shouldReportSupportedAnnotationTypes() {
		Set<String> supportedTypes = processor.getSupportedAnnotationTypes();

		assertTrue(supportedTypes.contains("eu.nerdfactor.restness.annotation.RestnessController"));
		assertTrue(supportedTypes.contains("eu.nerdfactor.restness.annotation.RestnessSecurity"));
		assertTrue(supportedTypes.contains("eu.nerdfactor.restness.annotation.RestnessConfiguration"));
		assertEquals(3, supportedTypes.size());
	}
}
