package eu.nerdfactor.restness.processing.extractor;

/**
 * Provides type-safe extraction of single values from annotations.
 *
 * @param <T> The type of value to extract
 */
public class TypedValueExtractor<T> implements TypedAnnotationValue<T> {
	private final Class<T> type;
	private final String key;
	private final AnnotationValueExtractor extractor;
	private T value;

	TypedValueExtractor(Class<T> type, String key, AnnotationValueExtractor extractor) {
		this.type = type;
		this.key = key;
		this.extractor = extractor;
	}

	@Override
	public T getValue() {
		if (value == null) {
			value = extractor.extract()
					.getValue(key, type)
					.orElse(null);
		}
		return value;
	}

	@Override
	public String getKey() {
		return key;
	}
}