package eu.nerdfactor.restness.data;

/**
 * A data transfer object of a specific entity that is capable of converting
 * itself into an entity.
 *
 * @param <E> Type of the entity for this object.
 * @author Daniel Klug
 */
public interface DataTransferObject<E> {

	/**
	 * Convert the dto to the matching entity.
	 *
	 * @return The converted entity.
	 */
	E convertToEntity();
}
