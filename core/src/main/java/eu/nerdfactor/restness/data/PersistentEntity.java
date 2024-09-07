package eu.nerdfactor.restness.data;

/**
 * A persistent entity that is capable to convert itself into a specific data
 * transfer object and be merged with an updated version of itself.
 *
 * @param <D> Type of the data transfer object for this entity.
 * @author Daniel Klug
 */
public interface PersistentEntity<D> {

	/**
	 * Convert the entity to the matching dto.
	 *
	 * @return The converted dto.
	 */
	D convertToDto();

	/**
	 * Merge the entity with a dto by converting the dto to a matching entity.
	 *
	 * @param dto The dto to merge with.
	 * @param <E> The type of the entity.
	 * @return The merged entity.
	 */
	default <E> E mergeWithDto(DataTransferObject<E> dto) {
		return mergeWithEntity(dto.convertToEntity());
	}

	/**
	 * Merge with an entity.
	 *
	 * @param entity The entity to merge with.
	 * @param <E>    The type of the entity.
	 * @return The merged entity.
	 */
	default <E> E mergeWithEntity(E entity) {
		return entity;
	}
}
