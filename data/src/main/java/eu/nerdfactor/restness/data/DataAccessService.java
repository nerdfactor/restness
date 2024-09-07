package eu.nerdfactor.restness.data;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Generic way to access entity data from a service.
 *
 * @param <E>  Type of the entity.
 * @param <ID> Type of the entity's id.
 * @author Daniel Klug
 */
public interface DataAccessService<E, ID> extends DataAccessor<E, ID> {

	/**
	 * Get the repository used to access the entities.
	 *
	 * @return A {@link CrudRepository} for data access.
	 */
	CrudRepository<E, ID> getRepository();

	/**
	 * List all entities.
	 *
	 * @return An Iterable of entities.
	 */
	default Iterable<E> listData() {
		return this.getRepository().findAll();
	}

	/**
	 * Searches for data, filtered by the {@link Specification} and restricted
	 * to a {@link Page}. Filtering and paging is only applied if the repository
	 * implements the corresponding interfaces. Otherwise, a page with all data
	 * will be returned.
	 *
	 * @param spec The {@link Specification} for filtering.
	 * @param page The {@link Pageable} for paging.
	 * @return A filtered and paged set of data.
	 */
	@SuppressWarnings("unchecked")
	default Page<E> searchData(Specification<E> spec, Pageable page) {
		CrudRepository<E, ID> repository = this.getRepository();
		if (repository instanceof JpaSpecificationExecutor) {
			JpaSpecificationExecutor<E> executor = (JpaSpecificationExecutor<E>) repository;
			return executor.findAll(spec, page);
		}
		if (repository instanceof PagingAndSortingRepository) {
			PagingAndSortingRepository<E, ID> pagingAndSorting = (PagingAndSortingRepository<E, ID>) repository;
			return pagingAndSorting.findAll(page);
		}
		return new DataPage<>(StreamSupport
				.stream(this.getRepository().findAll().spliterator(), false)
				.toList());
	}

	/**
	 * Create a new entity with the provided data.
	 *
	 * @param entity The new entity.
	 * @return The same entity after it was created.
	 */
	default E createData(@NotNull E entity) {
		return this.updateData(entity);
	}

	/**
	 * Read the entity specified by the id.
	 *
	 * @param id The id of the entity.
	 * @return An {@link Optional} of the read entity.
	 */
	default Optional<E> readData(ID id) {
		return this.getRepository().findById(id);
	}

	/**
	 * Update the provided entity.
	 *
	 * @param entity The entity with updated data.
	 * @return The same entity after it was updated.
	 */
	default E updateData(@NotNull E entity) {
		return this.getRepository().save(entity);
	}

	/**
	 * Delete the provided entity.
	 *
	 * @param entity The entity to delete.
	 */
	default void deleteData(@NotNull E entity) {
		this.getRepository().delete(entity);
	}

	/**
	 * Delete the entity with the specified id.
	 *
	 * @param id The id of the entity to delete.
	 */
	default void deleteDataById(@NotNull ID id) {
		this.getRepository().deleteById(id);
	}
}
