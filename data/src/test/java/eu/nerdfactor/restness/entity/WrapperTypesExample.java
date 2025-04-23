package eu.nerdfactor.restness.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * Test class for wrapper types merging.
 */
@Getter
@Setter
public class WrapperTypesExample {
	private Integer integerValue;
	private Long longValue;
	private Double doubleValue;
	private Boolean booleanValue;
	private Character charValue;

	public WrapperTypesExample(Integer integerValue, Long longValue, Double doubleValue,
	                           Boolean booleanValue, Character charValue) {
		this.integerValue = integerValue;
		this.longValue = longValue;
		this.doubleValue = doubleValue;
		this.booleanValue = booleanValue;
		this.charValue = charValue;
	}
}
