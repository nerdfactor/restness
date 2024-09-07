package eu.nerdfactor.restness.config;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.restness.util.RestnessUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Security configuration for controller generation.
 *
 * @author Daniel Klug
 */
@Getter
public class SecurityConfiguration {

	ClassName className;

	/**
	 * Pattern for roles that will be checked on rest methods.
	 * <li>METHOD: Type of method - CREATE, READ, UPDATE, DELETE</li>
	 * <li>ENTITY: Name of the entity.</li>
	 * <li>Name: Name of relation or entity.</li>
	 */
	String pattern = "ROLE_{METHOD}_{ENTITY}";

	/**
	 * The security checks can include the base check on relations. The user
	 * has to have READ permissions for the base element to access any relations
	 * of it and UPDATE permissions to change a relation.
	 */
	boolean inclusive = true;

	public String getRole(String method, String entity, String name) {
		return this.pattern
				.replace("{METHOD}", method)
				.replace("{ENTITY}", RestnessUtil.normalizeEntityName(entity))
				.replace("{NAME}", name)
				.toUpperCase();
	}

	public String getSecurityString(TypeName entity, TypeName relation, String method, String methodBase) {
		String relationEntityName = RestnessUtil.toClassName(relation).simpleName();
		String relationRole = this.getRole(method, relationEntityName, relationEntityName);
		String security = "hasRole('" + relationRole + "')";
		if (this.inclusive) {
			String baseEntityName = RestnessUtil.toClassName(entity).simpleName();
			String baseRole = this.getRole(methodBase, baseEntityName, baseEntityName);
			security += " and hasRole('" + baseRole + "')";
		}
		return security;
	}

	public SecurityConfiguration(){}

	public SecurityConfiguration(ClassName className, String pattern, boolean inclusive) {
		this.className = className;
		this.pattern = pattern;
		this.inclusive = inclusive;
	}

	public static @NotNull SecurityConfigurationBuilder builder() {
		return new SecurityConfigurationBuilder();
	}

}
