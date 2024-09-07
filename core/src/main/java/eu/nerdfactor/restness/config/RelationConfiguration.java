package eu.nerdfactor.restness.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.restness.util.WordInflector;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for relation generation.
 *
 * @author Daniel Klug
 */
@Getter
@Setter
public class RelationConfiguration {

	private String name;

	private RelationType type;

	private String getter;

	private String setter;

	private String adder;

	private String remover;

	private ClassName entityClass;

	private TypeName dtoClass;

	private boolean withDtos;

	private TypeName idClass;

	private String idAccessor;

	public void setAccessors(String[] accessors) {
		this.setGetter(accessors[0]);
		this.setSetter(accessors[1]);
		this.setAdder(accessors[2]);
		this.setRemover(accessors[3]);
	}

	public void setDtoClass(TypeName dtoClass) {
		this.dtoClass = dtoClass;
		this.withDtos = dtoClass != null;
	}

	@JsonIgnore
	public TypeName getResponse() {
		return this.withDtos && this.dtoClass != null && !this.dtoClass.equals(TypeName.OBJECT) ? this.dtoClass : this.entityClass;
	}

	public String getMethodName(AccessorType type) {
		String methodName = this.name.substring(0, 1).toUpperCase() + this.name.substring(1);
		String singularName = WordInflector.getInstance().singularize(methodName);
		return switch (type) {
			case GET -> "get" + methodName;
			case SET -> "set" + methodName;
			case ADD -> "add" + singularName;
			case REMOVE -> "remove" + singularName;
		};
	}

	public static RelationConfigurationBuilder builder() {
		return new RelationConfigurationBuilder();
	}

}
