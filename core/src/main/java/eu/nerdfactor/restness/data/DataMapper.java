package eu.nerdfactor.restness.data;

/**
 * Generic way to convert between entity and dto.
 * ModelMapper, Orika and Dozer don't implement DataMapper
 * but the signature for map() matches and can be used during
 * code generation.
 *
 * @author Daniel Klug
 */
public interface DataMapper {

	/**
	 * Convert between entity and dto.
	 *
	 * @param obj The original object.
	 * @param cls The class of the converted object.
	 * @return The converted object.
	 */
	public <T> T map(Object obj, Class<T> cls);
}