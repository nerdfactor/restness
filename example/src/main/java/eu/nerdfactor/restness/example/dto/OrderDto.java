package eu.nerdfactor.restness.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.nerdfactor.restness.example.customer.CustomerDao;
import eu.nerdfactor.restness.example.entity.ProductEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Basic Dto for Orders.
 */
@Setter
@Getter
public class OrderDto {

	private int id;

	private LocalDateTime orderedAt;

	private int amount;

	@JsonIgnore
	private CustomerDao customer;

	@JsonIgnore
	private List<ProductEntity> products;

}
