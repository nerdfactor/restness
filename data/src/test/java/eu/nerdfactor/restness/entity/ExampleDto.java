package eu.nerdfactor.restness.entity;

import eu.nerdfactor.restness.data.DataTransferObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExampleDto implements DataTransferObject<Example> {

	public int id;
	public String name;
	public boolean active;
	public long amount;

	@Override
	public Example convertToEntity() {
		return new Example(this.id, this.name, this.active, this.amount);
	}
}
