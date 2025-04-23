package eu.nerdfactor.restness.data;

import eu.nerdfactor.restness.entity.Example;
import eu.nerdfactor.restness.entity.ExampleDto;
import eu.nerdfactor.restness.entity.WrapperTypesExample;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class RestnessEntityMergerTest {

	private final DataMerger merger = new RestnessEntityMerger();

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
}