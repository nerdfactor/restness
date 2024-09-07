package eu.nerdfactor.restness.code.builder;

import org.jetbrains.annotations.NotNull;

/**
 * A buildable object that is able to build from a specific configuration object.
 *
 * @param <T> The type of the configuration object.
 */
public interface Configurable<T> {

	/**
	 * Set the configuration object that will be used to build.
	 *
	 * @param configuration The configuration object.
	 * @return The object in a fluent api pattern.
	 */
	Configurable<T> withConfiguration(@NotNull T configuration);
}
