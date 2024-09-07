package eu.nerdfactor.restness.data;

import org.springframework.data.jpa.domain.Specification;

/**
 * Generic way to build a Specification from a filter string.
 *
 * @author Daniel Klug
 */
public interface DataSpecificationBuilder {

	/**
	 * Build a {@link Specification} from a filter string.
	 *
	 * @param filter The filter string.
	 * @param cls    The class to filter.
	 * @param <T>    The type of the filtered class.
	 * @return A new {@link Specification} to filter a class.
	 */
	<T> Specification<T> build(String filter, Class<T> cls);
}
