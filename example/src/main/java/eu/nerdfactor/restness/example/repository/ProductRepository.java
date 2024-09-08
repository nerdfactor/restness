package eu.nerdfactor.restness.example.repository;

import eu.nerdfactor.restness.example.entity.ProductEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * Repository for Products.<br> Implements {@link PagingAndSortingRepository} in
 * order to provide paging and sorting. The controller will not be able to
 * filter results.<br> Instead of security on the controllers the products are
 * secured directly in the repository.
 */
@Repository
public interface ProductRepository extends PagingAndSortingRepository<ProductEntity, Integer>, CrudRepository<ProductEntity, Integer> {

	@NotNull
	@Secured("ROLE_READ_PRODUCT")
	Optional<ProductEntity> findById(@NotNull Integer id);

	@Secured("ROLE_READ_PRODUCT")
	boolean existsById(@NotNull Integer id);

	@NotNull
	@Override
	@Secured("ROLE_READ_PRODUCT")
	Iterable<ProductEntity> findAll(@NotNull Sort sort);

	@NotNull
	@Override
	@Secured("ROLE_READ_PRODUCT")
	Page<ProductEntity> findAll(@NotNull Pageable pageable);

	@NotNull
	@Secured("ROLE_UPDATE_PRODUCT")
	<S extends ProductEntity> S save(@NotNull S entity);

	@Secured("ROLE_DELETE_PRODUCT")
	void deleteById(@NotNull Integer id);

	@Secured("ROLE_DELETE_PRODUCT")
	void delete(@NotNull ProductEntity entity);
}
