package eu.nerdfactor.restness.example.customer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.nerdfactor.restness.example.entity.Employee;
import eu.nerdfactor.restness.example.entity.OrderModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

/**
 * Basic Dto for Customers.
 */
@Getter
@Setter
public class CustomerDto {

	private String email;

	private String name;

	@JsonIgnore
	private Collection<OrderModel> orders;

	@JsonIgnore
	private Employee support;
}
