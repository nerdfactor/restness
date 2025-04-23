package eu.nerdfactor.restness.config;

import com.squareup.javapoet.ClassName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Security Configuration for RESTness controller generation.
 *
 * @author Daniel Klug
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class SecurityConfiguration {

	/**
	 * The {@link ClassName} of the controller this configuration is for. This
	 * includes the full namespace and the name of the class.
	 *
	 * <li>{@code com.example.controller.ProductRestController}</li>
	 * <li>{@code com.example.controller.UserController}</li>
	 * <li>{@code com.example.controller.GeneratedOrderController}</li>
	 */
	protected ClassName controllerClassName;

	/**
	 * The pattern used for the role to restrict access of controller action.
	 * The pattern can include placeholders for:
	 * <li>METHOD: Type of the method - CREATE, READ, UPDATE, DELETE</li>
	 * <li>ENTITY: Name of the entity.</li>
	 * <li>NAME: Name of relation or entity.</li>
	 */
	@Builder.Default
	protected String securityRolePattern = "ROLE_{METHOD}_{ENTITY}";

	/**
	 * Decide if the actions to access relations only require the permissions
	 * for the related entity. Otherwise, they also require the READ or UPDATE
	 * permission for the base entity.
	 */
	@Builder.Default
	protected boolean inclusiveRelationPermissions = true;
}
