package eu.nerdfactor.restness.code.injector;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
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
		if (this.relatedClassName != null) {
			security = this.securityConfig.getSecurityExpression(this.entityClassName, this.relatedClassName, this.method, this.method);
		} else {
			ClassName entityName = RestnessUtil.toClassName(this.entityClassName);
			String role = this.securityConfig.getSecurityRole(this.method, entityName.simpleName(), entityName.simpleName());
			security = "hasRole('" + role + "')";
		}
		builder.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		return builder;
	}
}
