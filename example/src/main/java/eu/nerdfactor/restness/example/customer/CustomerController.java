package eu.nerdfactor.restness.example.customer;

import eu.nerdfactor.restness.annotation.RestnessController;
import eu.nerdfactor.restness.annotation.RestnessSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Base controller for Customers.<br> Uses RestnessController to configure a
 * generated controller based on the controller. Provides information about the
 * entity, id, and dto to use in the generated controller.
 * <br>
 * Uses RestnessSecurity to configure Spring security for the generated
 * controller.
 */
@RestController
@RequiredArgsConstructor
@RestnessController(value = "/api/customers", entity = CustomerDao.class, id = String.class, dto = CustomerDto.class)
@RestnessSecurity
public class CustomerController {

	private final CustomerRepository repository;

	/**
	 * Implements a delete method that will be used instead of a method in the
	 * generated controller. As long as the {@link RequestMapping} matches the
	 * generated one, it will not be created.
	 *
	 * @param id The id of the customer.
	 * @return A fitting ResponseEntity.
	 */
	@DeleteMapping("/api/customers/{id}")
	@PreAuthorize("hasRole('ROLE_DELETE_CUSTOMER')")
	public ResponseEntity<Void> delete(@PathVariable final String id) {
		this.repository.deleteDataById(id);
		return ResponseEntity.noContent().build();
	}
}
