package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.builder.Buildable;
import eu.nerdfactor.restness.config.ControllerConfiguration;

/**
 * SPI interface for providing custom method builders that are discovered
 * via {@link java.util.ServiceLoader} and added to the controller generation
 * pipeline after the built-in builders.
 * <p>
 * To register a custom method builder provider, implement this interface
 * and add the fully qualified class name to
 * {@code META-INF/services/eu.nerdfactor.restness.code.methodbuilder.RestnessMethodBuilderProvider}.
 *
 * @author Daniel Klug
 * @see MethodBuilderRegistry
 */
public interface RestnessMethodBuilderProvider {

	/**
	 * Returns a human-readable name for this provider, used in log messages.
	 *
	 * @return The name of this provider.
	 */
	String getName();

	/**
	 * Returns the execution order of this provider. Lower values execute first.
	 * Defaults to {@link Integer#MAX_VALUE} so custom builders run after
	 * all built-in builders.
	 *
	 * @return The order value.
	 */
	default int getOrder() {
		return Integer.MAX_VALUE;
	}

	/**
	 * Creates a configured {@link Buildable} that will add methods to the
	 * controller's {@link TypeSpec.Builder}.
	 *
	 * @param configuration The {@link ControllerConfiguration} for the controller being generated.
	 * @return A configured builder ready to be executed.
	 */
	Buildable<TypeSpec.Builder> createBuilder(ControllerConfiguration configuration);
}
