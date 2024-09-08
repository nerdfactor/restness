package eu.nerdfactor.restness.entity;

import eu.nerdfactor.restness.data.PersistentEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Example implements PersistentEntity<ExampleDto> {

	public int id;
	public String name;
	public boolean active;
	public long amount;

	@Override
	public ExampleDto convertToDto() {
		return new ExampleDto(this.id, this.name, this.active, this.amount);
	}

	@Override
	public PersistentEntity<ExampleDto> mergeWithEntity(PersistentEntity<?> persistent) {
		if (persistent instanceof Example entity) {
			this.id = entity.getId();
			this.name = entity.getName();
			this.active = entity.isActive();
			this.amount = entity.getAmount();
		}
		return this;
	}


}
