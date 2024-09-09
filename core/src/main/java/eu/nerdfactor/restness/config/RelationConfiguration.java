package eu.nerdfactor.restness.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.restness.util.WordInflector;
import lombok.*;

/**
 * Relation Configuration for RESTness controller generation.
 *
 * @author Daniel Klug
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class RelationConfiguration {

	/**
	 * The name of the relationship between entities.
	 */
	protected String relationName;

	/**
	 * The {@link RelationType} between entities.
	 */
	protected RelationType relationType;

	/**
	 * The method name to get the related entity.
	 */
	protected String getterMethodName;

	/**
	 * The method name to set the related entity.
	 */
	protected String setterMethodName;

	/**
	 * The method name to add to a list of related entities.
	 */
	protected String adderMethodName;

	/**
	 * The method name remove from a list of related entities.
	 */
	protected String removerMethodName;

	/**
	 * The {@link ClassName} for the entity managed by the relationship. This
	 * includes the full namespace and the name of the class.
	 *
	 * <li>{@code com.example.entity.Product}</li>
	 * <li>{@code com.example.entity.UserEntity}</li>
	 * <li>{@code com.example.model.OrderModel}</li>
	 */
	protected ClassName entityClassName;

	/**
	 * The {@link TypeName} for the returned object from the relationship. This
	 * includes the full namespace and the name of the class.
	 * <p>
	 * The response object can be the same type as the entity (just keep the
	 * default value) or some different type if the relationship should use data
	 * transfer objects.
	 *
	 * <li>{@code com.example.entity.Product}</li>
	 * <li>{@code com.example.dto.UserDto}</li>
	 * <li>{@code com.example.viewmodel.OrderViewModel}</li>
	 */
	@Builder.Default
	protected TypeName responseObjectClassName = TypeName.OBJECT;

	/**
	 * The {@link TypeName} for the entities id field. This includes the full
	 * namespace and the name of the class. For primitive id types like int or
	 * long the Java object should be used. This is necessary due to the use of
	 * generics in Spring JPAs repositories (i.e. CrudRepository<T, ID>).
	 *
	 * <li>{@code java.lang.Integer}</li>
	 * <li>{@code java.lang.String}</li>
	 * <li>{@code java.util.UUID}</li>
	 */
	protected TypeName idClassName;

	/**
	 * The method name to access the id of the entity. This will be used to get
	 * the id of entities.
	 *
	 * <li>{@code getId}</li>
	 * <li>{@code getIdentifier}</li>
	 * <li>{@code getOrderNumber}</li>
	 */
	@Builder.Default
	protected String idAccessorMethodName = "getId";

	/**
	 * Get the {@link TypeName} of a response object.
	 *
	 * @return The {@link TypeName} of a response object.
	 */
	@JsonIgnore
	public TypeName getResponseType() {
		return this.isUsingDto() ? this.responseObjectClassName : this.entityClassName;
	}

	/**
	 * Check if the relationship uses data transfer objects.
	 *
	 * @return True if the relationship uses data transfer objects.
	 */
	@JsonIgnore
	public boolean isUsingDto() {
		return this.responseObjectClassName != null && !this.responseObjectClassName.equals(TypeName.OBJECT);
	}

	/**
	 * Set all access method names for the relationship.
	 *
	 * @param accessorMethodNames An array of method names.
	 */
	public void setAccessorMethodNames(String... accessorMethodNames) {
		if (accessorMethodNames.length > 0) {
			this.setGetterMethodName(accessorMethodNames[0]);
		}
		if (accessorMethodNames.length > 1) {
			this.setSetterMethodName(accessorMethodNames[1]);
		}
		if (accessorMethodNames.length > 2) {
			this.setAdderMethodName(accessorMethodNames[2]);
		}
		if (accessorMethodNames.length > 3) {
			this.setRemoverMethodName(accessorMethodNames[3]);
		}
		if (accessorMethodNames.length > 4) {
			this.setIdAccessorMethodName(accessorMethodNames[4]);
		}
	}

	public String getMethodName(AccessorType type) {
		String methodName = this.relationName.substring(0, 1).toUpperCase() + this.relationName.substring(1);
		String singularName = WordInflector.getInstance().singularize(methodName);
		return switch (type) {
			case GET -> "get" + methodName;
			case SET -> "set" + methodName;
			case ADD -> "add" + singularName;
			case REMOVE -> "remove" + singularName;
		};
	}
}
