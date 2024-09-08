package eu.nerdfactor.restness.example.customer;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.nerdfactor.restness.annotation.RelationAccessor;
import eu.nerdfactor.restness.config.AccessorType;
import eu.nerdfactor.restness.example.entity.Employee;
import eu.nerdfactor.restness.example.entity.OrderModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

/**
 * Basic entity for Customers.<br> Uses Dao suffix style. Dto will be assumed to
 * be called CustomerDto.
 */
@Setter
@Getter
@Entity
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CustomerDao {

	@Id
	private String email;

	private String name;

	@OneToMany
	private Collection<OrderModel> orders;

	@OneToOne
	private Employee support;

	/**
	 * Uses RelationAccessor to provide access to add orders.
	 */
	@RelationAccessor(name = "orders", type = AccessorType.ADD)
	public void addOrder(OrderModel order) {
		this.orders.add(order);
	}

	public void addOrderModel(OrderModel order) {
		this.orders.add(order);
	}

	/**
	 * Uses RelationAccessor to provide access to remove orders.
	 */
	@RelationAccessor(name = "orders", type = AccessorType.REMOVE)
	public void removeOrder(OrderModel order) {
		this.orders.remove(order);
	}

	public void removeOrderModel(OrderModel order) {
		this.orders.remove(order);
	}

}
