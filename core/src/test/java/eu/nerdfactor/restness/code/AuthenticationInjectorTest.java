package eu.nerdfactor.restness.code;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import eu.nerdfactor.restness.code.injector.AuthenticationInjector;
import eu.nerdfactor.restness.config.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationInjectorTest {

	@Test
	void shouldNotInjectWhenNoSecurityConfig() {
		MethodSpec.Builder method = MethodSpec.methodBuilder("testMethod");

		new AuthenticationInjector()
				.withMethod("READ")
				.withEntityClassName(ClassName.get("com.example", "Product"))
				.inject(method);

		String code = method.build().toString();
		assertFalse(code.contains("PreAuthorize"));
	}

	@Test
	void shouldInjectPreAuthorizeForReadMethod() {
		SecurityConfiguration config = SecurityConfiguration.builder()
				.withSecurityRolePattern("ROLE_{METHOD}_{ENTITY}")
				.build();

		MethodSpec.Builder method = MethodSpec.methodBuilder("testMethod");

		new AuthenticationInjector()
				.withMethod("READ")
				.withEntityClassName(ClassName.get("com.example", "Product"))
				.withSecurityConfig(config)
				.inject(method);

		String code = method.build().toString();
		assertTrue(code.contains("PreAuthorize"));
		assertTrue(code.contains("ROLE_READ_PRODUCT"));
	}

	@Test
	void shouldInjectPreAuthorizeForCreateMethod() {
		SecurityConfiguration config = SecurityConfiguration.builder()
				.withSecurityRolePattern("ROLE_{METHOD}_{ENTITY}")
				.build();

		MethodSpec.Builder method = MethodSpec.methodBuilder("testMethod");

		new AuthenticationInjector()
				.withMethod("CREATE")
				.withEntityClassName(ClassName.get("com.example", "Product"))
				.withSecurityConfig(config)
				.inject(method);

		String code = method.build().toString();
		assertTrue(code.contains("ROLE_CREATE_PRODUCT"));
	}

	@Test
	void shouldNormalizeEntityNameSuffix() {
		SecurityConfiguration config = SecurityConfiguration.builder()
				.withSecurityRolePattern("ROLE_{METHOD}_{ENTITY}")
				.build();

		MethodSpec.Builder method = MethodSpec.methodBuilder("testMethod");

		new AuthenticationInjector()
				.withMethod("READ")
				.withEntityClassName(ClassName.get("com.example", "ProductEntity"))
				.withSecurityConfig(config)
				.inject(method);

		String code = method.build().toString();
		assertTrue(code.contains("ROLE_READ_PRODUCT"));
	}

	@Test
	void shouldUseCustomRolePattern() {
		SecurityConfiguration config = SecurityConfiguration.builder()
				.withSecurityRolePattern("PERM_{METHOD}_{ENTITY}")
				.build();

		MethodSpec.Builder method = MethodSpec.methodBuilder("testMethod");

		new AuthenticationInjector()
				.withMethod("DELETE")
				.withEntityClassName(ClassName.get("com.example", "Order"))
				.withSecurityConfig(config)
				.inject(method);

		String code = method.build().toString();
		assertTrue(code.contains("PERM_DELETE_ORDER"));
	}
}
