package eu.nerdfactor.restness.data;

import eu.nerdfactor.restness.entity.Example;
import eu.nerdfactor.restness.entity.ExampleDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RestnessEntityMergerTest {

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
	 */
	@Test
	void shouldMergeObjectsUsingReflection() {
		ExampleDto original = new ExampleDto(1, "original", true, 10L);
		ExampleDto updated = new ExampleDto(1, "updated", false, 100L);

		ExampleDto result = this.merger.merge(original, updated);

		Assertions.assertEquals("updated", result.getName());
		Assertions.assertEquals(100L, result.getAmount());
		Assertions.assertFalse(result.isActive());
		Assertions.assertSame(original, result);
	}

	/**
	 * Test that two objects of the same type are merged using reflection. Null
	 * values should be ignored.
	 */
	@Test
	void shouldPartiallyMergeObjectsWithNullUsingReflection() {
		ExampleDto original = new ExampleDto(1, "original", true, 10L);
		ExampleDto updated = new ExampleDto(1, null, false, 100L);

		ExampleDto result = this.merger.merge(original, updated);

		Assertions.assertEquals("original", result.getName());
		Assertions.assertEquals(100L, result.getAmount());
		Assertions.assertFalse(result.isActive());
		Assertions.assertSame(original, result);
	}

	/**
	 * Test that two objects of the same type are merged using reflection. Empty
	 * Strings should be ignored.
	 */
	@Test
	void shouldPartiallyMergeObjectsWithEmptyStringUsingReflection() {
		ExampleDto original = new ExampleDto(1, "original", true, 10L);
		ExampleDto updated = new ExampleDto(1, "", false, 100L);

		ExampleDto result = this.merger.merge(original, updated);

		Assertions.assertEquals("original", result.getName());
		Assertions.assertEquals(100L, result.getAmount());
		Assertions.assertFalse(result.isActive());
		Assertions.assertSame(original, result);
	}
}
