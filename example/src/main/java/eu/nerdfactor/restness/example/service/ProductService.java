package eu.nerdfactor.restness.example.service;

import eu.nerdfactor.restness.data.DataAccessService;
import eu.nerdfactor.restness.example.entity.ProductEntity;
import eu.nerdfactor.restness.example.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

/**
 * Service for Products.<br> Implements DataAccessService to provide normalized
 * access to entities.
 */
@Service
@RequiredArgsConstructor
public class ProductService implements DataAccessService<ProductEntity, Integer> {

	private final ProductRepository repository;

	@Override
	public CrudRepository<ProductEntity, Integer> getRepository() {
		return this.repository;
	}
}
