package eu.nerdfactor.restness.processing.extractor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AnnotationValueExtractor} which validates the extraction of values
 * from Java annotations during compile-time annotation processing. These tests verify
 * the ability to extract simple values, arrays, nested annotations, and handle error cases.
 */
@ExtendWith(MockitoExtension.class)
class AnnotationValueExtractorTest {

	/**
	 * Mock element representing an annotated class, method, or field
	 */
	@Mock
	private Element element;

	/**
	 * Mock elements utility for accessing annotation details
	 */
	@Mock
	private Elements utils;

	/**
	 * Mock representation of an annotation instance
	 */
	@Mock
	private AnnotationMirror annotationMirror;

	/**
	 * Mock representation of a nested annotation
	 */
	@Mock
	private AnnotationMirror nestedAnnotationMirror;

	/**
	 * Mock representation of an annotation method
	 */
	@Mock
	private ExecutableElement executableElement;

	/**
	 * Mock representation of an annotation value
	 */
	@Mock
	private AnnotationValue annotationValue;

	/**
	 * Mock type representation of an annotation
	 */
	@Mock
	private DeclaredType declaredType;

	/**
	 * The extractor instance being tested
	 */
	private AnnotationValueExtractor extractor;

	/**
	 * Set up a fresh extractor instance before each test.
	 */
	@BeforeEach
	void setUp() {
		// Create new instance for each test to ensure clean state
		extractor = new AnnotationValueExtractor();
	}

	/**
	 * Tests extraction of a simple key-value pair from an annotation.
	 * Verifies that the extractor can correctly:
	 * - Find the target annotation on an element
	 * - Extract a simple string value
	 * - Store it with the correct key in the result map
	 */
	@Test
	void shouldExtractSimpleAnnotationValue() {
		// Test configuration values
		String className = "test.annotation.Class";
		String expectedValue = "testValue";

		// Mock the annotation lookup chain:
		// Element -> AnnotationMirror -> DeclaredType -> className
		// This simulates finding our target annotation on the element
		doReturn(List.of(annotationMirror))
				.when(element).getAnnotationMirrors();
		doReturn(declaredType)
				.when(annotationMirror).getAnnotationType();
		doReturn(className)
				.when(declaredType).toString();

		// Create a map of annotation values that links the member (executableElement)
		// to its value (annotationValue). This represents the annotation's data structure
		Map<ExecutableElement, AnnotationValue> elementValues = new HashMap<>();
		elementValues.put(executableElement, annotationValue);

		// Mock the annotation processor utilities to return our prepared values
		doReturn(elementValues)
				.when(utils).getElementValuesWithDefaults(any());

		// Set up the annotation member name that will become our map key
		doReturn(new TestName("testKey"))
				.when(executableElement).getSimpleName();

		// Set up the actual value that should be extracted
		doReturn(expectedValue)
				.when(annotationValue).getValue();

		// Execute the extraction process using our builder pattern
		AnnotationValueExtractor.ValueWrapper result = extractor
				.forClass(className)
				.withElement(element)
				.withUtils(utils)
				.extractUnsafe();

		// Verify the extraction was successful and contained expected data
		assertNotNull(result);
		// Check that the value was stored with the correct key
		assertEquals(expectedValue, result.values().get("testKey"));
		// Verify the result contains reference to original element
		assertEquals(element, result.element());
		// Verify the annotation class name was preserved
		assertEquals(className, result.annotationClassName());
	}

	/**
	 * Tests the extraction of array values from annotations.
	 * Currently verifies the placeholder behavior for array handling (TODO).
	 * When array support is implemented, this test should be updated to verify
	 * proper array value extraction.
	 */
	@Test
	void shouldExtractArrayValue() {
		// Test configuration
		String className = "test.annotation.Class";
		String[] arrayValue = new String[]{"value1", "value2"};

		// Mock the annotation lookup chain
		// This follows same pattern as simple value test for finding the annotation
		doReturn(List.of(annotationMirror))
				.when(element).getAnnotationMirrors();
		doReturn(declaredType)
				.when(annotationMirror).getAnnotationType();
		doReturn(className)
				.when(declaredType).toString();

		// Set up the array value in the annotation structure
		Map<ExecutableElement, AnnotationValue> elementValues = new HashMap<>();
		elementValues.put(executableElement, annotationValue);
		doReturn(elementValues)
				.when(utils).getElementValuesWithDefaults(any());

		// Set the member name that should hold the array
		doReturn(new TestName("arrayKey"))
				.when(executableElement).getSimpleName();

		// Configure the mock to return our test array
		doReturn(arrayValue)
				.when(annotationValue).getValue();

		// Perform the extraction
		AnnotationValueExtractor.ValueWrapper result = extractor
				.forClass(className)
				.withElement(element)
				.withUtils(utils)
				.extractUnsafe();

		// Verify the result object exists but contains no values
		// as array handling is not yet implemented (marked with TODO in implementation)
		assertNotNull(result);
		assertTrue(result.values().isEmpty());
	}

	/**
	 * Tests extraction of nested annotation values with path-based keys.
	 * Verifies that the extractor can:
	 * - Navigate through nested annotation structures
	 * - Build proper key paths (e.g., "outer/inner")
	 * - Extract values from the deepest level
	 */
	@Test
	void shouldExtractNestedAnnotationWithPrefixPath() {
		// Test configuration
		String className = "test.annotation.Class";
		String nestedValue = "nestedValue";

		// Set up the basic annotation structure (same as other tests)
		doReturn(List.of(annotationMirror))
				.when(element).getAnnotationMirrors();
		doReturn(declaredType)
				.when(annotationMirror).getAnnotationType();
		doReturn(className)
				.when(declaredType).toString();

		// Create a wrapped annotation mirror that represents a nested annotation value
		AnnotationValue wrappedNestedMirror = mock(AnnotationValue.class);
		doReturn(nestedAnnotationMirror)
				.when(wrappedNestedMirror).getValue();

		// Configure the outer annotation to contain the nested annotation
		Map<ExecutableElement, AnnotationValue> outerValues = new HashMap<>();
		outerValues.put(executableElement, wrappedNestedMirror);
		doReturn(outerValues)
				.when(utils).getElementValuesWithDefaults(annotationMirror);

		// Set up the nested annotation's internal structure
		Map<ExecutableElement, AnnotationValue> nestedValues = new HashMap<>();
		ExecutableElement nestedElement = mock(ExecutableElement.class);
		AnnotationValue finalValue = mock(AnnotationValue.class);
		nestedValues.put(nestedElement, finalValue);
		doReturn(nestedValues)
				.when(utils).getElementValuesWithDefaults(nestedAnnotationMirror);

		// Configure the value in the deepest level and the path components
		doReturn(nestedValue)
				.when(finalValue).getValue();
		// Set up the path parts that should be joined with "/"
		doReturn(new TestName("outer"))
				.when(executableElement).getSimpleName();
		doReturn(new TestName("inner"))
				.when(nestedElement).getSimpleName();

		// Perform the nested extraction
		AnnotationValueExtractor.ValueWrapper result = extractor
				.forClass(className)
				.withElement(element)
				.withUtils(utils)
				.extractUnsafe();

		// Verify the nested structure was correctly traversed
		assertNotNull(result);
		// Check that the nested value was stored with a path-based key
		assertEquals(nestedValue, result.values().get("outer/inner"));
		// Verify we accessed the nested value through proper chain
		verify(wrappedNestedMirror).getValue();
	}

	/**
	 * Tests extraction of annotation values that contain a list of complex objects.
	 * Verifies that the extractor can:
	 * - Handle collections of annotation mirrors
	 * - Extract values from each item in the collection
	 * - Return a list of properly populated ValueWrapper objects
	 */
	@Test
	void shouldExtractListOfComplexAnnotationValues() {
		// Given
		String className = "test.annotation.Class";
		String nestedValue1 = "nestedValue1";
		String nestedValue2 = "nestedValue2";

		// Setup basic annotation structure
		doReturn(List.of(annotationMirror))
				.when(element).getAnnotationMirrors();

		doReturn(declaredType)
				.when(annotationMirror).getAnnotationType();

		doReturn(className)
				.when(declaredType).toString();

		// Create two nested annotation mirrors with different values
		AnnotationMirror nestedMirror1 = mock(AnnotationMirror.class);
		AnnotationMirror nestedMirror2 = mock(AnnotationMirror.class);
		List<AnnotationMirror> mirrors = Arrays.asList(nestedMirror1, nestedMirror2);

		// Setup element values map that returns a List
		Map<ExecutableElement, AnnotationValue> elementValues = new HashMap<>();
		elementValues.put(executableElement, annotationValue);
		doReturn(elementValues)
				.when(annotationMirror).getElementValues();

		doReturn(mirrors)
				.when(annotationValue).getValue();

		// Setup nested value extraction for first mirror
		Map<ExecutableElement, AnnotationValue> nestedValues1 = new HashMap<>();
		ExecutableElement nestedElement1 = mock(ExecutableElement.class);
		AnnotationValue nestedValueObj1 = mock(AnnotationValue.class);
		nestedValues1.put(nestedElement1, nestedValueObj1);
		doReturn(nestedValues1)
				.when(utils).getElementValuesWithDefaults(eq(nestedMirror1));

		// Setup nested value extraction for second mirror
		Map<ExecutableElement, AnnotationValue> nestedValues2 = new HashMap<>();
		ExecutableElement nestedElement2 = mock(ExecutableElement.class);
		AnnotationValue nestedValueObj2 = mock(AnnotationValue.class);
		nestedValues2.put(nestedElement2, nestedValueObj2);
		doReturn(nestedValues2)
				.when(utils).getElementValuesWithDefaults(eq(nestedMirror2));

		// Setup names and values
		doReturn(new TestName("value"))
				.when(nestedElement1).getSimpleName();
		doReturn(new TestName("value"))
				.when(nestedElement2).getSimpleName();
		doReturn(nestedValue1)
				.when(nestedValueObj1).getValue();
		doReturn(nestedValue2)
				.when(nestedValueObj2).getValue();

		// When
		List<AnnotationValueExtractor.ValueWrapper> results = extractor
				.forClass(className)
				.withElement(element)
				.withUtils(utils)
				.extractListUnsafe();

		// Then
		assertNotNull(results);
		assertEquals(2, results.size());
		assertEquals(nestedValue1, results.get(0).values().get("value"));
		assertEquals(nestedValue2, results.get(1).values().get("value"));
	}

	/**
	 * Tests error handling during value extraction.
	 * Verifies that the extractor:
	 * - Catches exceptions during value extraction
	 * - Returns an empty but valid result
	 * - Doesn't propagate the exception to the caller
	 */
	@Test
	void shouldHandleExceptionDuringValueExtraction() {
		// Given
		String className = "test.annotation.Class";

		// Setup basic annotation structure
		doReturn(List.of(annotationMirror))
				.when(element).getAnnotationMirrors();

		doReturn(declaredType)
				.when(annotationMirror).getAnnotationType();

		doReturn(className)
				.when(declaredType).toString();

		// Setup value extraction to throw exception
		Map<ExecutableElement, AnnotationValue> elementValues = new HashMap<>();
		elementValues.put(executableElement, annotationValue);
		doReturn(elementValues)
				.when(utils).getElementValuesWithDefaults(any());

		doReturn(new TestName("key"))
				.when(executableElement).getSimpleName();

		doThrow(new RuntimeException("Test exception"))
				.when(annotationValue).getValue();

		// When
		AnnotationValueExtractor.ValueWrapper result = extractor
				.forClass(className)
				.withElement(element)
				.withUtils(utils)
				.extractUnsafe();

		// Then
		assertNotNull(result);
		assertTrue(result.values().isEmpty()); // Verify exception is caught and handled
	}

	/**
	 * Tests behavior when no matching annotation is found.
	 * Verifies that the extractor:
	 * - Handles the case of missing annotations gracefully
	 * - Returns an empty but valid result
	 */
	@Test
	void shouldHandleNullAnnotationMirror() {
		// Given
		String className = "test.annotation.NonExistent";

		// Return empty list to simulate no matching annotation
		doReturn(Collections.emptyList())
				.when(element).getAnnotationMirrors();

		// When
		AnnotationValueExtractor.ValueWrapper result = extractor
				.forClass(className)
				.withElement(element)
				.withUtils(utils)
				.extractUnsafe();

		// Then
		assertNotNull(result);
		assertTrue(result.values().isEmpty());
	}

	/**
	 * Helper class that implements the Name interface for testing.
	 * Used to create mock annotation member names in a type-safe way.
	 */
	private static class TestName implements javax.lang.model.element.Name {
		private final String name;

		TestName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public int length() {
			return name.length();
		}

		@Override
		public char charAt(int index) {
			return name.charAt(index);
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return name.subSequence(start, end);
		}

		@Override
		public boolean contentEquals(CharSequence cs) {
			return name.contentEquals(cs);
		}
	}
}
