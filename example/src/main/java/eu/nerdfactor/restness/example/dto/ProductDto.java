package eu.nerdfactor.restness.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.nerdfactor.restness.data.DataTransferObject;
import eu.nerdfactor.restness.example.entity.OrderModel;
import eu.nerdfactor.restness.example.entity.ProductEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic Dto for Products.<br> Implements DataTransferObject to provide a method
 * for mapping into the matching entity.
 */
@Setter
@Getter
public class ProductDto implements DataTransferObject<ProductEntity> {

	private int id;

	private String name;

	@JsonIgnore
	private Set<OrderModel> orders;

	@Override
	public ProductEntity convertToEntity() {
		ProductEntity productEntity = new ProductEntity();
		productEntity.setId(this.id);
		productEntity.setName(this.name);
		productEntity.setOrders(this.orders);
		return productEntity;
	}

	public ProductDto() {
		this.orders = new HashSet<>();
	}
}
