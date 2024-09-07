package eu.nerdfactor.restness.data;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.stream.StreamSupport;

/**
 * Generic way to access entity data from a service.
 *
 * @param <E>  Type of the entity.
 * @param <ID> Type of the entity's id.
 * @author Daniel Klug
 */
public interface DataAccessService<E, ID> extends DataAccessor<E, ID> {

	CrudRepository<E, ID> getRepository();

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

	default E createData(@NotNull E entity) {
		return this.updateData(entity);
	}

	default E readData(ID id) {
		return this.getRepository().findById(id).orElse(null);
	}

	default E updateData(@NotNull E entity) {
		return this.getRepository().save(entity);
	}

	default void deleteData(@NotNull E entity) {
		this.getRepository().delete(entity);
	}

	default void deleteDataById(@NotNull ID id) {
		this.getRepository().deleteById(id);
	}
}
