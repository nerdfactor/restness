package eu.nerdfactor.restness.code.injector;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.restness.config.SecurityConfiguration;
import eu.nerdfactor.restness.util.RestnessUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * An injector that adds authentication to a method.
 *
 * @author Daniel Klug
 */
public class AuthenticationInjector implements Injectable<MethodSpec.Builder> {

	/**
	 * The access method required by the guard.
	 */
	protected String method = "READ";

	/**
	 * The class of the guarded entity.
	 */
	protected TypeName entityClassName;

	/**
	 * The class of the guarded related entity.
	 */
	protected TypeName relatedClassName;

	/**
	 * The {@link SecurityConfiguration} for basic security configurations.
	 */
	protected SecurityConfiguration securityConfig;

	/**
	 * @param method The access method.
	 * @return The injector in a fluent api pattern.
	 */
	public AuthenticationInjector withMethod(@NotNull String method) {
		this.method = method.trim().toUpperCase();
		return this;
	}

	/**
	 * @param entity The type of the entity.
	 * @return The injector in a fluent api pattern.
	 */
	public AuthenticationInjector withEntityClassName(TypeName entity) {
		this.entityClassName = entity;
		return this;
	}

	/**
	 * @param relatedEntity The type of the related entity.
	 * @return The injector in a fluent api pattern.
	 */
	public AuthenticationInjector withRelatedClassName(TypeName relatedEntity) {
		this.relatedClassName = relatedEntity;
		return this;
	}

	/**
	 * @param config The used {@link SecurityConfiguration}.
	 * @return The injector in a fluent api pattern.
	 */
	public AuthenticationInjector withSecurityConfig(SecurityConfiguration config) {
		this.securityConfig = config;
		return this;
	}

	/**
	 * Inject into a {@link MethodSpec.Builder} and add authentication
	 * annotations.
	 *
	 * @param builder An existing builder object that will be used.
	 * @return The altered {@link MethodSpec.Builder}.
	 */
	public MethodSpec.Builder inject(MethodSpec.Builder builder) {
		if (this.securityConfig == null) {
			return builder;
		}
		// todo: separate relationship into separate injector or find a way to combine the underlying role generation.
		String security = "";
		String baseEntityClassName = RestnessUtil.toClassName(this.entityClassName).simpleName();
		if (this.relatedClassName != null) {
			String baseRealtedClassName = RestnessUtil.toClassName(this.relatedClassName).simpleName();
			security = this.getSecurityExpression(baseEntityClassName, baseRealtedClassName, this.method, this.method);
		} else {
			String role = this.getSecurityRole(this.method, baseEntityClassName, baseEntityClassName);
			security = "hasRole('" + role + "')";
		}
		builder.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		return builder;
	}

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
		return this.securityConfig.getSecurityRolePattern()
				.replace("{METHOD}", method)
				.replace("{ENTITY}", RestnessUtil.normalizeEntityName(entity))
				.replace("{NAME}", name)
				.toUpperCase();
	}

	public String getSecurityExpression(String baseEntityName, String relationEntityName, String method, String methodBase) {
		String relationRole = this.getSecurityRole(method, relationEntityName, relationEntityName);
		String security = "hasRole('" + relationRole + "')";
		if (this.securityConfig.isInclusiveRelationPermissions()) {
			String baseRole = this.getSecurityRole(methodBase, baseEntityName, baseEntityName);
			security += " and hasRole('" + baseRole + "')";
		}
		return security;
	}
}
