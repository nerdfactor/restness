package eu.nerdfactor.restness.data;

/**
 * A persistent entity that is capable to convert itself
 * into a specific data transfer object and be merged
 * with an updated version of itself.
 *
 * @param <D> Type of the data transfer object for this entity.
 * @author Daniel Klug
 */
public interface PersistentEntity<D> {

	public D convertToDto();

	default <E> E mergeWithDto(DataTransferObject<E> dto) {
		return mergeWithEntity(dto.convertToEntity());
	}

	default <E> E mergeWithEntity(E entity) {
		return entity;
	}
}
