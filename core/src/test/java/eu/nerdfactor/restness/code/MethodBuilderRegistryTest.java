package eu.nerdfactor.restness.code;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.builder.Buildable;
import eu.nerdfactor.restness.code.methodbuilder.MethodBuilderRegistry;
import eu.nerdfactor.restness.code.methodbuilder.RestnessMethodBuilderProvider;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MethodBuilderRegistryTest {

	@Test
	void shouldCreateEmptyRegistryWhenNoSpiProviders() {
		MethodBuilderRegistry registry = new MethodBuilderRegistry();
		ControllerConfiguration config = ControllerConfiguration.builder()
				.withControllerClassName(ClassName.get("com.example", "TestController"))
				.build();

		List<Buildable<TypeSpec.Builder>> builders = registry.createBuilders(config);
		assertTrue(builders.isEmpty());
	}

	@Test
	void shouldReturnBuildersFromRegisteredProviders() {
		MethodBuilderRegistry registry = new MethodBuilderRegistry(
				List.of(new TestMethodBuilderProvider("test", Integer.MAX_VALUE)));

		ControllerConfiguration config = ControllerConfiguration.builder()
				.withControllerClassName(ClassName.get("com.example", "TestController"))
				.build();

		List<Buildable<TypeSpec.Builder>> builders = registry.createBuilders(config);
		assertEquals(1, builders.size());
	}

	@Test
	void shouldSortProvidersByOrder() {
		TestMethodBuilderProvider high = new TestMethodBuilderProvider("high", 100);
		TestMethodBuilderProvider low = new TestMethodBuilderProvider("low", 1);
		MethodBuilderRegistry registry = new MethodBuilderRegistry(List.of(high, low));

		ControllerConfiguration config = ControllerConfiguration.builder()
				.withControllerClassName(ClassName.get("com.example", "TestController"))
				.build();

		List<Buildable<TypeSpec.Builder>> builders = registry.createBuilders(config);
		assertEquals(2, builders.size());
	}

	@Test
	void shouldPassConfigurationToProvider() {
		ConfigCapturingProvider provider = new ConfigCapturingProvider();
		MethodBuilderRegistry registry = new MethodBuilderRegistry(List.of(provider));

		ControllerConfiguration config = ControllerConfiguration.builder()
				.withControllerClassName(ClassName.get("com.example", "TestController"))
				.withRequestBasePath("/api/test")
				.build();

		registry.createBuilders(config);
		assertSame(config, provider.capturedConfig);
	}

	private static class TestMethodBuilderProvider implements RestnessMethodBuilderProvider {
		private final String name;
		private final int order;

		TestMethodBuilderProvider(String name, int order) {
			this.name = name;
			this.order = order;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public int getOrder() {
			return order;
		}

		@Override
		public Buildable<TypeSpec.Builder> createBuilder(ControllerConfiguration configuration) {
			return builder -> builder;
		}
	}

	private static class ConfigCapturingProvider implements RestnessMethodBuilderProvider {
		ControllerConfiguration capturedConfig;

		@Override
		public String getName() {
			return "config-capturing";
		}

		@Override
		public Buildable<TypeSpec.Builder> createBuilder(ControllerConfiguration configuration) {
			this.capturedConfig = configuration;
			return builder -> builder;
		}
	}
}
