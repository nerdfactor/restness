package eu.nerdfactor.restness.data;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Example;
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

	/**
	 * Checks if an entity with the given ID exists.
	 *
	 * @param id must not be {@literal null}.
	 * @return {@literal true} if an entity with the given ID exists,
	 *         {@literal false} otherwise.
	 * @throws IllegalArgumentException if {@literal id} is {@literal null}.
	 */
	boolean existsDataById(@NotNull ID id);

	/**
	 * Checks whether the data store contains elements that match the given
	 * {@link Specification}.
	 *
	 * @param spec the {@link Specification} to check for. Can be {@literal null}.
	 * @return {@literal true} if the data store contains elements that match the
	 *         given {@link Specification}, {@literal false} otherwise.
	 */
	boolean existsData(Specification<E> spec);

	/**
	 * Checks whether the data store contains elements that match the given
	 * {@link Example}.
	 *
	 * @param example the {@link Example} to check for. Must not be {@literal null}.
	 * @return {@literal true} if the data store contains elements that match the
	 *         given {@link Example}, {@literal false} otherwise.
	 * @throws IllegalArgumentException if {@literal example} is {@literal null}.
	 */
	boolean existsData(Example<E> example);
}
