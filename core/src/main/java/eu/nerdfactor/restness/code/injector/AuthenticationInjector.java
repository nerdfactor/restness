package eu.nerdfactor.restness.code.injector;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.restness.config.SecurityConfiguration;
import eu.nerdfactor.restness.util.RestnessUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;

public class AuthenticationInjector implements Injectable<MethodSpec.Builder> {

	protected String method = "READ";

	protected TypeName type;

	protected TypeName relation;

	protected SecurityConfiguration securityConfig;

	public AuthenticationInjector withMethod(@NotNull String method) {
		this.method = method.trim().toUpperCase();
		return this;
	}

	public AuthenticationInjector withType(TypeName type) {
		this.type = type;
		return this;
	}

	public AuthenticationInjector withRelation(TypeName relation) {
		this.relation = relation;
		return this;
	}

	public AuthenticationInjector withSecurityConfig(SecurityConfiguration config) {
		this.securityConfig = config;
		return this;
	}

	public MethodSpec.Builder inject(MethodSpec.Builder builder) {
		if (this.securityConfig == null) {
			return builder;
		}
		// todo: separate relationship into separate injector or find a way to combine the underlying role generation.
		String security = "";
		if (this.relation != null) {
			security = this.securityConfig.getSecurityExpression(this.type, this.relation, this.method, this.method);
		} else {
			ClassName entityName = RestnessUtil.toClassName(this.type);
			String role = this.securityConfig.getSecurityRole(this.method, entityName.simpleName(), entityName.simpleName());
			security = "hasRole('" + role + "')";
		}
		builder.addAnnotation(AnnotationSpec.builder(PreAuthorize.class).addMember("value", "$S", security).build());
		return builder;
	}
}
