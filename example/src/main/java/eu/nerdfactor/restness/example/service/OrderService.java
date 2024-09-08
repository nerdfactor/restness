package eu.nerdfactor.restness.example.service;

import eu.nerdfactor.restness.data.DataAccessService;
import eu.nerdfactor.restness.example.entity.OrderModel;
import eu.nerdfactor.restness.example.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

/**
 * Service for Orders.<br> Implements DataAccessService to provide normalized
 * access to entities.
 */
@Service
@RequiredArgsConstructor
public class OrderService implements DataAccessService<OrderModel, Integer> {

	private final OrderRepository repository;

	@Override
	public CrudRepository<OrderModel, Integer> getRepository() {
		return this.repository;
	}
}
