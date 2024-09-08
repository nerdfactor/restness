package eu.nerdfactor.restness.example.entity;

import eu.nerdfactor.restness.annotation.Relation;
import eu.nerdfactor.restness.data.PersistentEntity;
import eu.nerdfactor.restness.example.dto.ProductDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic entity for Products.<br> Uses Entity suffix style. Dto will be assumed
 * to be called ProductDto.
 * <br>
 * Implements PersistentEntity to provide a method for mapping into the matching
 * dto.
 */

@Entity
@Getter
@Setter
public class ProductEntity implements PersistentEntity<ProductDto> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String name;

	/**
	 * Uses Relation to specify a relation to Products.<br> The annotation is
	 * used to provide names of the accessor methods to add and remove objects
	 * from the relation.
	 */
	@Relation(name = "orders", add = "sendOrder", remove = "cancelOrder")
	@ManyToMany(fetch = FetchType.EAGER)
	private Set<OrderModel> orders;

	public void sendOrder(OrderModel order) {
		this.orders.add(order);
	}

	public void cancelOrder(OrderModel order) {
		this.orders.remove(order);
	}

	@Override
	public ProductDto convertToDto() {
		return null;
	}

	public ProductEntity() {
		this.orders = new HashSet<>();
	}
}
