package eu.nerdfactor.restness.data;

import eu.nerdfactor.restness.entity.Example;
import eu.nerdfactor.restness.entity.ExampleDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RestnessEntityMapperTest {

	private final DataMapper mapper = new RestnessEntityMapper();

	/**
	 * Test that the entity is mapped to a matching dto.
	 */
	@Test
	void shouldMapEntityToDto() {
		Example entity = new Example(1, "original", true, 10L);
		ExampleDto result = this.mapper.map(entity, ExampleDto.class);
		Assertions.assertInstanceOf(ExampleDto.class, result);
	}

	/**
	 * Test that the dto is mapped to a matching entity.
	 */
	@Test
	void shouldMapDtoToEntity() {
		ExampleDto dto = new ExampleDto(1, "original", true, 10L);
		Example result = this.mapper.map(dto, Example.class);
		Assertions.assertInstanceOf(Example.class, result);
	}

	/**
	 * Test that mapping between two unknown objects won't work.
	 */
	@Test
	void shouldNotMapObjectToDifferentObject() {
		String entity = "test";
		Assertions.assertThrows(RuntimeException.class,
				() -> this.mapper.map(entity, Integer.class)
		);
	}
}
