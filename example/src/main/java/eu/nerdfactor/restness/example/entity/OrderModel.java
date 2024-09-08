package eu.nerdfactor.restness.example.entity;

import eu.nerdfactor.restness.annotation.Relation;
import eu.nerdfactor.restness.example.customer.CustomerDao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Basic entity for Orders.<br> Uses Model suffix style. Dto will be assumed to
 * be called OrderDto.
 */
@Setter
@Getter
@Entity
public class OrderModel {

	@Id
	private int id;

	private LocalDateTime orderedAt;

	private int amount;

	@ManyToOne
	private CustomerDao customer;

	/**
	 * Uses Relation to specify a relation to Products. This may not be
	 * necessary because of ManyToMany annotation being present.
	 */
	@Relation
	@ManyToMany(fetch = FetchType.EAGER)
	private List<ProductEntity> products;

	public void addProduct(ProductEntity product) {
		this.products.add(product);
	}

	public void removeProduct(ProductEntity product) {
		this.products.remove(product);
	}
}
