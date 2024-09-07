package eu.nerdfactor.restness.data;

import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;

/**
 * A simplistic specification builder that will directly map a filter string
 * into a specification for query filtering.
 * Filter String should look like:
 * name1:value1;name2:value2;name3:value3
 *
 * @author Daniel Klug
 */
public class RestnessSpecificationBuilder implements DataSpecificationBuilder {

	/**
	 * Build a {@link Specification} from a filter string.
	 *
	 * @param filter The filter string.
	 * @param cls    The class to filter.
	 * @param <T>    The type of the filtered class.
	 * @return A new {@link Specification} to filter a class.
	 */
	@Override
	public <T> Specification<T> build(String filter, Class<T> cls) {
		Specification<T> spec = Specification.where(null);
		if (filter != null && !filter.isBlank()) {
			try {
				Arrays.stream(filter.split(";")).forEach(pair -> {
					String[] params = pair.split(":");
					spec.and((root, query, cb) -> cb.equal(root.get(params[0]), params[1]));
				});
			} catch (Exception e) {
				// ignore wrong formatted filters.
			}
		}
		return spec;
	}

}
