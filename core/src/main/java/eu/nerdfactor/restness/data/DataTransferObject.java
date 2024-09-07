package eu.nerdfactor.restness.data;

/**
 * A data transfer object of a specific entity that is
 * capable of converting itself into an entity.
 *
 * @param <E> Type of the entity for this object.
 * @author Daniel Klug
 */
public interface DataTransferObject<E> {

	public E convertToEntity();
}
