package eu.nerdfactor.restness.example.extension;

import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.builder.Buildable;
import eu.nerdfactor.restness.code.methodbuilder.RestnessMethodBuilderProvider;
import eu.nerdfactor.restness.config.ControllerConfiguration;

/**
 * Example SPI provider that registers the {@link CountEndpointBuilder} for
 * automatic discovery via {@link java.util.ServiceLoader}.
 * <p>
 * This demonstrates how to implement {@link RestnessMethodBuilderProvider}
 * to add custom endpoints to all generated controllers. The provider is
 * registered in {@code META-INF/services} and discovered automatically
 * during code generation.
 *
 * @author Daniel Klug
 * @see RestnessMethodBuilderProvider
 * @see CountEndpointBuilder
 */
public class CountEndpointBuilderProvider implements RestnessMethodBuilderProvider {

	@Override
	public String getName() {
		return "count-endpoint";
	}

	@Override
	public int getOrder() {
		return Integer.MAX_VALUE;
	}

	/**
	 * Creates a {@link CountEndpointBuilder} configured for the given controller.
	 *
	 * @param configuration The controller configuration.
	 * @return A configured builder that will add the count endpoint.
	 */
	@Override
	public Buildable<TypeSpec.Builder> createBuilder(ControllerConfiguration configuration) {
		return CountEndpointBuilder.fromConfiguration(configuration);
	}
}
