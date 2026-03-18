package eu.nerdfactor.restness.code.injector;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.restness.config.SecurityConfiguration;
import eu.nerdfactor.restness.util.RestnessUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * An injector that adds relationship-level authentication to a method.
 * Generates composite security expressions that may require permissions
 * for both the related entity and the base entity when
 * {@link SecurityConfiguration#isInclusiveRelationPermissions()} is true.
 *
 * @author Daniel Klug
 */
public class RelationAuthenticationInjector extends AuthenticationInjector {

	/**
	 * The class of the guarded related entity.
	 */
	protected TypeName relatedClassName;

	/**
	 * @param relatedEntity The type of the related entity.
	 * @return The injector in a fluent api pattern.
	 */
	public RelationAuthenticationInjector withRelatedClassName(TypeName relatedEntity) {
		this.relatedClassName = relatedEntity;
		return this;
	}

	@Override
	public RelationAuthenticationInjector withMethod(@NotNull String method) {
		super.withMethod(method);
		return this;
	}

	@Override
	public RelationAuthenticationInjector withEntityClassName(TypeName entity) {
		super.withEntityClassName(entity);
		return this;
	}

	@Override
	public RelationAuthenticationInjector withSecurityConfig(SecurityConfiguration config) {
		super.withSecurityConfig(config);
		return this;
	}

	/**
	 * Inject into a {@link MethodSpec.Builder} and add relationship-level
	 * authentication annotations with composite security expressions.
	 *
	 * @param builder An existing builder object that will be used.
	 * @return The altered {@link MethodSpec.Builder}.
	 */
	@Override
	public MethodSpec.Builder inject(MethodSpec.Builder builder) {
		if (this.securityConfig == null) {
			return builder;
		}
		String baseEntityName = RestnessUtil.toClassName(this.entityClassName).simpleName();
		String relationEntityName = RestnessUtil.toClassName(this.relatedClassName).simpleName();
		String security = this.getSecurityExpression(baseEntityName, relationEntityName);
		builder.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		return builder;
	}

	/**
	 * Build a composite security expression for relationship access.
	 * When inclusive relation permissions are enabled, requires roles
	 * for both the related entity and the base entity.
	 *
	 * @param baseEntityName     The name of the base entity.
	 * @param relationEntityName The name of the related entity.
	 * @return A Spring security expression.
	 */
	protected String getSecurityExpression(String baseEntityName, String relationEntityName) {
		String relationRole = this.getSecurityRole(this.method, relationEntityName, relationEntityName);
		String security = "hasRole('" + relationRole + "')";
		if (this.securityConfig.isInclusiveRelationPermissions()) {
			String baseRole = this.getSecurityRole(this.method, baseEntityName, baseEntityName);
			security += " and hasRole('" + baseRole + "')";
		}
		return security;
	}
}
