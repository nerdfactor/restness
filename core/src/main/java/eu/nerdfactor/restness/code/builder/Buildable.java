package eu.nerdfactor.restness.code.builder;

/**
 * A buildable object used with a builder object.
 *
 * @param <T> The type of object buildable object.
 */
public interface Buildable<T> {

	/**
	 * Build the object.
	 *
	 * @param builder An existing builder object that will be used.
	 * @return The builder object.
	 */
	T buildWith(T builder);
}
