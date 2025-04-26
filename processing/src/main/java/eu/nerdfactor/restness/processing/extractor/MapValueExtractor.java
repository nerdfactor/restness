package eu.nerdfactor.restness.processing.extractor;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides type-safe extraction of map values from annotations.
 *
 * @param <K> The type of keys in the map
 * @param <V> The type of values in the map
 */
public class MapValueExtractor<K, V> implements TypedAnnotationValue<Map<K, V>> {
	private final Class<K> keyType;
	private final Class<V> valueType;
	private final String key;
	private final AnnotationValueExtractor extractor;
	private Map<K, V> value;

	MapValueExtractor(Class<K> keyType, Class<V> valueType, String key, AnnotationValueExtractor extractor) {
		this.keyType = keyType;
		this.valueType = valueType;
		this.key = key;
		this.extractor = extractor;
	}

	@Override
	public Map<K, V> getValue() {
		if (value == null) {
			value = extractor.extract()
					.getValue(key, Map.class)
					.map(map -> ((Map<?, ?>) map).entrySet().stream()
							.filter(e -> keyType.isInstance(e.getKey()) && valueType.isInstance(e.getValue()))
							.collect(Collectors.toMap(
									e -> keyType.cast(e.getKey()),
									e -> valueType.cast(e.getValue())
							)))
					.orElse(Map.of());
		}
		return value;
	}

	@Override
	public String getKey() {
		return key;
	}
}