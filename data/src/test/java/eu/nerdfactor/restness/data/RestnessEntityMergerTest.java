package eu.nerdfactor.restness.data;

import eu.nerdfactor.restness.entity.Example;
import eu.nerdfactor.restness.entity.ExampleDto;
import eu.nerdfactor.restness.entity.ExampleRecord;
import eu.nerdfactor.restness.entity.WrapperTypesExample;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

class RestnessEntityMergerTest {

	private final DataMerger merger = new RestnessEntityMerger();

	/**
	 * Provides test scenarios for entity merging with different name values.
	 *
	 * @return Stream of arguments with [updatedName, expectedName]
	 */
	static Stream<Arguments> provideMergeScenarios() {
		return Stream.of(
				Arguments.of("updated", "updated"),   // Non-empty name should be merged
				Arguments.of(null, "original"),       // Null values should be ignored
				Arguments.of("", "original")          // Empty strings should be ignored
		);
	}

	/**
	 * Test that a {@link PersistentEntity} is merged with a different entity of
	 * the same type using the {@link PersistentEntity#mergeWithEntity}
	 * implementation.
	 */
	@Test
	void shouldMergePersistentEntityWithPersistentEntity() {
		Example original = new Example(1, "original", true, 10L);
		Example updated = new Example(1, "updated", false, 100L);

		Example result = this.merger.merge(original, updated);

		Assertions.assertEquals("updated", result.getName());
		Assertions.assertSame(original, result);
	}

	/**
	 * Test that two objects of the same type are merged using reflection.
	 * Different input types (valid string, null, empty string) should be handled appropriately.
	 */
	@ParameterizedTest(name = "Merge with {0} should result in {1}")
	@MethodSource("provideMergeScenarios")
	void shouldMergeObjectsUsingReflection(String updatedName, String expectedName) {
		ExampleDto original = new ExampleDto(1, "original", true, 10L);
		ExampleDto updated = new ExampleDto(1, updatedName, false, 100L);

		ExampleDto result = this.merger.merge(original, updated);

		Assertions.assertEquals(expectedName, result.getName());
		Assertions.assertEquals(100L, result.getAmount());
		Assertions.assertFalse(result.isActive());
		Assertions.assertSame(original, result);
	}

	/**
	 * Test that wrapper types (Number, Boolean, Character) are correctly merged.
	 */
	@Test
	void shouldMergeWrapperTypes() {
		// Create test class with all wrapper types
		WrapperTypesExample original = new WrapperTypesExample(
				10,
				20L,
				30.5,
				Boolean.TRUE,
				'A'
		);

		WrapperTypesExample updated = new WrapperTypesExample(
				100,
				200L,
				305.5,
				Boolean.FALSE,
				'Z'
		);

		WrapperTypesExample result = this.merger.merge(original, updated);

		// Verify all wrapper types are merged correctly
		Assertions.assertEquals(Integer.valueOf(100), result.getIntegerValue());
		Assertions.assertEquals(Long.valueOf(200L), result.getLongValue());
		Assertions.assertEquals(Double.valueOf(305.5), result.getDoubleValue());
		Assertions.assertEquals(Boolean.FALSE, result.getBooleanValue());
		Assertions.assertEquals(Character.valueOf('Z'), result.getCharValue());
		Assertions.assertSame(original, result);
	}

	/**
	 * Test that null wrapper types are not merged.
	 */
	@Test
	void shouldNotMergeNullWrapperTypes() {
		WrapperTypesExample original = new WrapperTypesExample(
				10,
				20L,
				30.5,
				Boolean.TRUE,
				'A'
		);

		WrapperTypesExample updated = new WrapperTypesExample(
				null, null, null, null, null
		);

		WrapperTypesExample result = this.merger.merge(original, updated);

		// Verify original values are retained when updated values are null
		Assertions.assertEquals(Integer.valueOf(10), result.getIntegerValue());
		Assertions.assertEquals(Long.valueOf(20L), result.getLongValue());
		Assertions.assertEquals(Double.valueOf(30.5), result.getDoubleValue());
		Assertions.assertEquals(Boolean.TRUE, result.getBooleanValue());
		Assertions.assertEquals(Character.valueOf('A'), result.getCharValue());
	}

	/**
	 * Test that Record fields are merged correctly: String, int, boolean, and Long
	 * are updated, while List (unsupported type) is preserved from the original.
	 */
	@Test
	void shouldMergeRecordFields() {
		ExampleRecord original = new ExampleRecord("original", 10, true, 100L, List.of("a", "b"));
		ExampleRecord updated = new ExampleRecord("updated", 20, false, 200L, List.of("c"));

		ExampleRecord result = this.merger.merge(original, updated);

		Assertions.assertEquals("updated", result.name());
		Assertions.assertEquals(20, result.value());
		Assertions.assertFalse(result.active());
		Assertions.assertEquals(200L, result.amount());
		Assertions.assertEquals(List.of("a", "b"), result.tags());
	}

	/**
	 * Test that unsupported types in Records (like List) are not merged.
	 * The original value is preserved.
	 */
	@Test
	void shouldNotMergeUnsupportedTypesInRecords() {
		List<String> originalTags = List.of("original");
		List<String> updatedTags = List.of("updated");
		ExampleRecord original = new ExampleRecord("name", 1, true, 10L, originalTags);
		ExampleRecord updated = new ExampleRecord("name", 1, true, 10L, updatedTags);

		ExampleRecord result = this.merger.merge(original, updated);

		Assertions.assertSame(originalTags, result.tags());
	}

	/**
	 * Test that an empty string in the updated Record does not overwrite
	 * the original value, consistent with POJO merge behavior.
	 */
	@Test
	void shouldNotMergeEmptyStringInRecord() {
		ExampleRecord original = new ExampleRecord("original", 10, true, 100L, List.of());
		ExampleRecord updated = new ExampleRecord("", 10, true, 100L, List.of());

		ExampleRecord result = this.merger.merge(original, updated);

		Assertions.assertEquals("original", result.name());
	}

	/**
	 * Test that primitive default values (0, false) in the updated Record DO
	 * overwrite the original. This is consistent with POJO merger behavior where
	 * primitive getters always return non-null boxed values.
	 */
	@Test
	void shouldMergePrimitiveDefaultValuesInRecord() {
		ExampleRecord original = new ExampleRecord("name", 42, true, 100L, List.of());
		ExampleRecord updated = new ExampleRecord("name", 0, false, 100L, List.of());

		ExampleRecord result = this.merger.merge(original, updated);

		Assertions.assertEquals(0, result.value());
		Assertions.assertFalse(result.active());
	}

	/**
	 * Test that merging Records returns a new instance, not the original or updated.
	 */
	@Test
	void shouldReturnNewInstanceWhenMergingRecords() {
		ExampleRecord original = new ExampleRecord("original", 10, true, 100L, List.of());
		ExampleRecord updated = new ExampleRecord("updated", 20, false, 200L, List.of());

		ExampleRecord result = this.merger.merge(original, updated);

		Assertions.assertNotSame(original, result);
		Assertions.assertNotSame(updated, result);
	}

	/**
	 * Test that null handling works correctly with Records:
	 * null original returns updated, null updated returns original.
	 */
	@Test
	void shouldHandleNullRecordInMerge() {
		ExampleRecord record = new ExampleRecord("name", 10, true, 100L, List.of());

		Assertions.assertSame(record, this.merger.merge(null, record));
		Assertions.assertSame(record, this.merger.merge(record, null));
	}
}