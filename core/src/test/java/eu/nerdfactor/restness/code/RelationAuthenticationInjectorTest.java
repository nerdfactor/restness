package eu.nerdfactor.restness.code;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import eu.nerdfactor.restness.code.injector.RelationAuthenticationInjector;
import eu.nerdfactor.restness.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RelationAuthenticationInjectorTest {

	@Test
	void shouldNotInjectWhenNoSecurityConfig() {
		MethodSpec.Builder method = MethodSpec.methodBuilder("testMethod");

		new RelationAuthenticationInjector()
				.withMethod("READ")
				.withEntityClassName(ClassName.get("com.example", "Customer"))
				.withRelatedClassName(ClassName.get("com.example", "Order"))
				.inject(method);

		String code = method.build().toString();
		assertFalse(code.contains("PreAuthorize"));
	}

	@Test
	void shouldInjectInclusiveRelationPermissions() {
		SecurityConfiguration config = SecurityConfiguration.builder()
				.withSecurityRolePattern("ROLE_{METHOD}_{ENTITY}")
				.withInclusiveRelationPermissions(true)
				.build();

		MethodSpec.Builder method = MethodSpec.methodBuilder("testMethod");

		new RelationAuthenticationInjector()
				.withMethod("READ")
				.withEntityClassName(ClassName.get("com.example", "Customer"))
				.withRelatedClassName(ClassName.get("com.example", "Order"))
				.withSecurityConfig(config)
				.inject(method);

		String code = method.build().toString();
		assertTrue(code.contains("PreAuthorize"));
		assertTrue(code.contains("ROLE_READ_ORDER"));
		assertTrue(code.contains("ROLE_READ_CUSTOMER"));
		assertTrue(code.contains(" and "));
	}

	@Test
	void shouldInjectNonInclusiveRelationPermissions() {
		SecurityConfiguration config = SecurityConfiguration.builder()
				.withSecurityRolePattern("ROLE_{METHOD}_{ENTITY}")
				.withInclusiveRelationPermissions(false)
				.build();

		MethodSpec.Builder method = MethodSpec.methodBuilder("testMethod");

		new RelationAuthenticationInjector()
				.withMethod("UPDATE")
				.withEntityClassName(ClassName.get("com.example", "Customer"))
				.withRelatedClassName(ClassName.get("com.example", "Order"))
				.withSecurityConfig(config)
				.inject(method);

		String code = method.build().toString();
		assertTrue(code.contains("ROLE_UPDATE_ORDER"));
		assertFalse(code.contains("ROLE_UPDATE_CUSTOMER"));
		assertFalse(code.contains(" and "));
	}

	@Test
	void shouldNormalizeRelationEntityNames() {
		SecurityConfiguration config = SecurityConfiguration.builder()
				.withSecurityRolePattern("ROLE_{METHOD}_{ENTITY}")
				.withInclusiveRelationPermissions(true)
				.build();

		MethodSpec.Builder method = MethodSpec.methodBuilder("testMethod");

		new RelationAuthenticationInjector()
				.withMethod("READ")
				.withEntityClassName(ClassName.get("com.example", "CustomerEntity"))
				.withRelatedClassName(ClassName.get("com.example", "OrderEntity"))
				.withSecurityConfig(config)
				.inject(method);

		String code = method.build().toString();
		assertTrue(code.contains("ROLE_READ_ORDER"));
		assertTrue(code.contains("ROLE_READ_CUSTOMER"));
	}
}
