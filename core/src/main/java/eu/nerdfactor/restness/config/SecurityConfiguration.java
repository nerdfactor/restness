package eu.nerdfactor.restness.config;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.restness.util.RestnessUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

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

	/**
	 * Get the Spring security ROLE guarding the set of method, entity and
	 * name.
	 *
	 * @param method The method that should be guarded against.
	 * @param entity The guarded entity.
	 * @param name   The name of the guarded entity.
	 * @return A Spring security ROLE.
	 */
	public String getSecurityRole(String method, String entity, String name) {
		return this.securityRolePattern
				.replace("{METHOD}", method)
				.replace("{ENTITY}", RestnessUtil.normalizeEntityName(entity))
				.replace("{NAME}", name)
				.toUpperCase();
	}

	public String getSecurityExpression(TypeName entityClassName, TypeName relatedEntityClassName, String method, String methodBase) {
		String relationEntityName = RestnessUtil.toClassName(relatedEntityClassName).simpleName();
		String relationRole = this.getSecurityRole(method, relationEntityName, relationEntityName);
		String security = "hasRole('" + relationRole + "')";
		if (this.inclusiveRelationPermissions) {
			String baseEntityName = RestnessUtil.toClassName(entityClassName).simpleName();
			String baseRole = this.getSecurityRole(methodBase, baseEntityName, baseEntityName);
			security += " and hasRole('" + baseRole + "')";
		}
		return security;
	}
}
