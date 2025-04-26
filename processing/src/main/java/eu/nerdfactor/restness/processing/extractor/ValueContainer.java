package eu.nerdfactor.restness.processing.extractor;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.lang.model.element.Element;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A strongly typed wrapper for annotation values with conversion capabilities.
 */
@Slf4j
public class ValueContainer {
	private final Map<String, Object> values;

	@Getter
	private final Element element;

	@Getter
	private final String annotationClassName;

	public ValueContainer(Element element, String annotationClassName) {
		this.element = element;
		this.annotationClassName = annotationClassName;
		this.values = new HashMap<>();
	}

	/**
	 * Checks if the container has any values.
	 */
	public boolean hasValues() {
		return !values.isEmpty();
	}

	/**
	 * Checks if the container has a specific key.
	 */
	public boolean hasValue(String key) {
		return values.containsKey(key);
	}

	/**
	 * Gets a value by key and attempts to convert it to the requested type.
	 */
	public <T> Optional<T> getValue(String key, Class<T> type) {
		Object value = values.get(key);
		if (value == null) {
			return Optional.empty();
		}

		if (type.isInstance(value)) {
			return Optional.of(type.cast(value));
		}

		// Handle numeric conversions
		if (Number.class.isAssignableFrom(type) && value instanceof Number) {
			return Optional.of(convertNumber((Number) value, type));
		}

		// Try valueOf method for enums and other types
		if (value instanceof String && type != String.class) {
			try {
				return Optional.of(convertViaValueOf(value.toString(), type));
			} catch (Exception e) {
				// Fall through to empty
			}
		}

		return Optional.empty();
	}

	/**
	 * Gets a type-safe list value.
	 */
	public <T> Optional<List<T>> getList(String key, Class<T> elementType) {
		Object value = values.get(key);
		if (!(value instanceof List<?> list)) {
			return Optional.empty();
		}

		List<T> result = list.stream()
				.filter(elementType::isInstance)
				.map(elementType::cast)
				.toList();

		return Optional.of(result);
	}

	/**
	 * Gets a type-safe map value.
	 */
	public <K, V> Optional<Map<K, V>> getMap(String key, Class<K> keyType, Class<V> valueType) {
		Object value = values.get(key);
		if (!(value instanceof Map<?, ?> map)) {
			return Optional.empty();
		}

		Map<K, V> result = new HashMap<>();

		map.forEach((k, v) -> {
			if (keyType.isInstance(k) && valueType.isInstance(v)) {
				result.put(keyType.cast(k), valueType.cast(v));
			}
		});

		return Optional.of(result);
	}

	/**
	 * Gets a string value.
	 */
	public Optional<String> getString(String key) {
		return getValue(key, String.class);
	}

	/**
	 * Gets a string value with default.
	 */
	public String getStringOrDefault(String key, String defaultValue) {
		return getString(key).orElse(defaultValue);
	}

	/**
	 * Gets an integer value.
	 */
	public Optional<Integer> getInteger(String key) {
		return getValue(key, Integer.class);
	}

	/**
	 * Gets a boolean value.
	 */
	public Optional<Boolean> getBoolean(String key) {
		return getValue(key, Boolean.class);
	}

	/**
	 * Gets all values as an unmodifiable map.
	 */
	public Map<String, Object> getValues() {
		return Collections.unmodifiableMap(values);
	}

	public Map<String, String> getStringValues() {
		Map<String, String> stringValues = new HashMap<>();
		for (Map.Entry<String, Object> entry : values.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			String stringValue;
			if (value == null) {
				stringValue = null;
			} else if (value instanceof Class<?>) {
				stringValue = ((Class<?>) value).getName();
			} else {
				stringValue = value.toString();
			}
			stringValues.put(key, stringValue);
		}
		return Collections.unmodifiableMap(stringValues);
	}

	/**
	 * Internal method to store a value.
	 */
	void putValue(String key, Object value) {
		values.put(key, value);
	}

	@SuppressWarnings("unchecked")
	private <T> T convertNumber(Number number, Class<T> targetType) {
		if (targetType == Byte.class) return (T) Byte.valueOf(number.byteValue());
		if (targetType == Short.class) return (T) Short.valueOf(number.shortValue());
		if (targetType == Integer.class) return (T) Integer.valueOf(number.intValue());
		if (targetType == Long.class) return (T) Long.valueOf(number.longValue());
		if (targetType == Float.class) return (T) Float.valueOf(number.floatValue());
		if (targetType == Double.class) return (T) Double.valueOf(number.doubleValue());
		throw new IllegalArgumentException("Unsupported number type: " + targetType);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private <T> T convertViaValueOf(String value, Class<T> type) throws Exception {
		// Handle enums
		if (type.isEnum()) {
			Class<? extends Enum> enumType = (Class<? extends Enum>) type;
			return (T) Enum.valueOf(enumType, value);
		}

		// Try valueOf method
		try {
			Method valueOf = type.getMethod("valueOf", String.class);
			return type.cast(valueOf.invoke(null, value));
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("No valueOf method found for type: " + type);
		}
	}
}
