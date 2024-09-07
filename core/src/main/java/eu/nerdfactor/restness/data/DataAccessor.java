package eu.nerdfactor.restness.data;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

/**
 * Generic way to access entity data.
 *
 * @param <E>  Type of the entity.
 * @param <ID> Type of the entity's id.
 * @author Daniel Klug
 */
public interface DataAccessor<E, ID> {

	/**
	 * List all entities.
	 *
	 * @return An Iterable of entities.
	 */
	Iterable<E> listData();

	/**
	 * Search all entities. Filter the result with a specification and contain
	 * them inside a page.
	 *
	 * @param spec A {@link Specification} to restrict the searched entities.
	 * @param page A {@link Pageable} to restrict the returned data.
	 * @return A Page of entities.
	 */
	Page<E> searchData(Specification<E> spec, Pageable page);

	/**
	 * Create a new entity with the provided data.
	 *
	 * @param entity The new entity.
	 * @return The same entity after it was created.
	 */
	E createData(@NotNull E entity);

	/**
	 * Read the entity specified by the id.
	 *
	 * @param id The id of the entity.
	 * @return An {@link Optional} of the read entity.
	 */
	Optional<E> readData(ID id);

	/**
	 * Update the provided entity.
	 *
	 * @param entity The entity with updated data.
	 * @return The same entity after it was updated.
	 */
	E updateData(@NotNull E entity);

	/**
	 * Delete the provided entity.
	 *
	 * @param entity The entity to delete.
	 */
	void deleteData(@NotNull E entity);

	/**
	 * Delete the entity with the specified id.
	 *
	 * @param id The id of the entity to delete.
	 */
	void deleteDataById(@NotNull ID id);
}
