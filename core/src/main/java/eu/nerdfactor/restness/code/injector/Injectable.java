package eu.nerdfactor.restness.code.injector;

import eu.nerdfactor.restness.code.builder.Buildable;

/**
 * A buildable object that can be injected into a builder object. Compared to a
 * {@link Buildable} an {@link Injectable} is used to alter builders that are
 * build somewhere else.
 *
 * @param <T> The type of builder object.
 */
public interface Injectable<T> {

	/**
	 * Inject into the object.
	 *
	 * @param builder An existing builder object that will be used.
	 * @return The builder object.
	 */
	T inject(T builder);
}
