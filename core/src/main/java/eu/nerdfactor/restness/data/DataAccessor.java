package eu.nerdfactor.restness.data;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Generic way to access entity data.
 *
 * @param <E> Type of the entity.
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
	 * Search all entities. Filter the result with a specification and
	 * contain them inside a page.
	 *
	 * @param spec
	 * @param page
	 * @return A Page of entities.
	 */
	Page<E> searchData(Specification<E> spec, Pageable page);

	/**
	 * Create a new entity with the provided data.
	 *
	 * @param entity
	 * @return
	 */
	E createData(@NotNull E entity);

	/**
	 * Read the entity specified by the id.
	 *
	 * @param id
	 * @return
	 */
	E readData(ID id);

	/**
	 * Update the provided entity.
	 *
	 * @param entity
	 * @return
	 */
	E updateData(@NotNull E entity);

	/**
	 * Delete the provided entity.
	 *
	 * @param entity
	 */
	void deleteData(@NotNull E entity);

	/**
	 * Delete the entity with the specified id.
	 *
	 * @param id
	 */
	void deleteDataById(@NotNull ID id);
}
