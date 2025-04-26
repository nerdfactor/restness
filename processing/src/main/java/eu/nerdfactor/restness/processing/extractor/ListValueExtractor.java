package eu.nerdfactor.restness.processing.extractor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides type-safe extraction of list values from annotations.
 *
 * @param <T> The type of elements in the list
 */
public class ListValueExtractor<T> implements TypedAnnotationValue<List<T>> {
	private final Class<T> elementType;
	private final String key;
	private final AnnotationValueExtractor extractor;
	private List<T> value;

	ListValueExtractor(Class<T> elementType, String key, AnnotationValueExtractor extractor) {
		this.elementType = elementType;
		this.key = key;
		this.extractor = extractor;
	}

	@Override
	public List<T> getValue() {
		if (value == null) {
			value = extractor.extract()
					.getValue(key, List.class)
					.map(list -> ((List<?>) list).stream()
							.filter(elementType::isInstance)
							.map(elementType::cast)
							.collect(Collectors.toList()))
					.orElse(List.of());
		}
		return value;
	}

	@Override
	public String getKey() {
		return key;
	}
}