package eu.nerdfactor.restness.data;

/**
 * Generic way to merge two entities of the same type
 * in order to get the updated values into the original
 * entity.
 *
 * @author Daniel Klug
 */
public interface DataMerger {

	public <T> T merge(T obj, T updated);
}
