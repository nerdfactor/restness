package eu.nerdfactor.restness.data;

/**
 * Generic way to merge two entities of the same type in order to get the
 * updated values into the original entity.
 *
 * @author Daniel Klug
 */
public interface DataMerger {

	/**
	 * Update an object by merging it with an updated version.
	 *
	 * @param original The original object.
	 * @param updated The object with updated values.
	 * @param <T> Type of the updated object.
	 * @return The original object with the updated values.
	 */
	<T> T merge(T original, T updated);
}
