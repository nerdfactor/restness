package eu.nerdfactor.restness.processing.extractor;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Extracts values from {@link Annotation Annotations} during annotation processing.
 *
 * @author Daniel Klug
 */
@Slf4j
public class AnnotationValueExtractor {

	/**
	 * The canonical name (with full package) of the annotation class that will
	 * be checked during extraction.
	 */
	protected String className;

	/**
	 * The element that had the annotation that will be checked.
	 */
	protected Element element;
	protected Elements utils;

	public AnnotationValueExtractor forClass(Class<? extends Annotation> annotationClass) {
		return this.forClass(annotationClass.getCanonicalName());
	}

	public AnnotationValueExtractor forClass(String className) {
		this.className = className;
		return this;
	}

	public AnnotationValueExtractor withElement(Element element) {
		this.element = element;
		return this;
	}

	public AnnotationValueExtractor withUtils(Elements utils) {
		this.utils = utils;
		return this;
	}

	/**
	 * Extracts values from an annotation into a new ValueContainer.
	 * Fully supports arrays and nested annotations.
	 *
	 * @return A ValueContainer with the extracted values
	 */
	public ValueContainer extract() {
		ValueContainer container = new ValueContainer(this.element, this.className);
		extractInto(container);
		return container;
	}

	/**
	 * Extracts values from an annotation into a list of ValueContainers.
	 * Used when the annotation contains a list or array of values.
	 *
	 * @return A list of ValueContainers with the extracted values
	 */
	public List<ValueContainer> extractList() {
		List<ValueContainer> containers = new ArrayList<>();
		final AnnotationMirror annotationMirror = getAnnotationMirror(element, className);

		if (annotationMirror != null) {
			Object value = annotationMirror.getElementValues().values().stream()
					.findFirst()
					.map(AnnotationValue::getValue)
					.orElse(null);

			if (value != null && (value instanceof Iterable<?> || value.getClass().isArray())) {
				extractIterableValues(annotationMirror, containers);
			} else {
				containers.add(extractSingleValue(annotationMirror));
			}
		}
		return containers;
	}

	/**
	 * Extracts values from an annotation into an existing ValueContainer.
	 *
	 * @param container The container to store the extracted values
	 */
	public void extractInto(ValueContainer container) {
		final AnnotationMirror annotationMirror = getAnnotationMirror(element, className);
		if (annotationMirror != null) {
			extractAnnotationValues(annotationMirror, container, "");
		}
	}

	private void extractIterableValues(AnnotationMirror annotationMirror, List<ValueContainer> containers) {
		annotationMirror.getElementValues().forEach((executableElement, annotationValue) -> {
			Object valueList = annotationValue.getValue();
			if (valueList instanceof Iterable<?> iter) {
				iter.forEach(mirror -> {
					if (mirror instanceof AnnotationMirror nestedMirror) {
						containers.add(extractSingleValue(nestedMirror));
					}
				});
			}
		});
	}

	private ValueContainer extractSingleValue(AnnotationMirror annotationMirror) {
		ValueContainer container = new ValueContainer(this.element, this.className);
		extractAnnotationValues(annotationMirror, container, "");
		return container;
	}

	private void extractAnnotationValues(AnnotationMirror annotationMirror, ValueContainer container, String prefix) {
		validateState();

		final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues =
				this.utils.getElementValuesWithDefaults(annotationMirror);

		elementValues.forEach((executableElement, annotationValue) -> {
			try {
				String key = buildKey(prefix, executableElement);
				Object value = extractValue(annotationValue);
				storeValue(key, value, container);
			} catch (Exception e) {
				log.debug("Error extracting annotation value: {}", e.getMessage());
			}
		});
	}

	private void validateState() {
		if (element == null) {
			throw new IllegalStateException("Element not set. Call withElement() first.");
		}
		if (className == null) {
			throw new IllegalStateException("Class name not set. Call forClass() first.");
		}
		if (utils == null) {
			throw new IllegalStateException("Utils not set. Call withUtils() first.");
		}
	}

	private String buildKey(String prefix, ExecutableElement element) {
		return prefix + element.getSimpleName();
	}

	private Object extractValue(AnnotationValue value) {
		if (value == null) return null;
		Object extracted = value.getValue();
		if (extracted instanceof List<?>) {
			return handleListValue((List<?>) extracted);
		}
		if (extracted != null && extracted.getClass().isArray()) {
			return handleArrayValue(extracted);
		}
		return extracted;
	}

	private List<?> handleListValue(List<?> list) {
		return list.stream()
				.map(item -> {
					if (item instanceof AnnotationValue) {
						return extractValue((AnnotationValue) item);
					}
					return item;
				})
				.toList();
	}

	private List<?> handleArrayValue(Object array) {
		if (array instanceof Object[] arr) {
			return Arrays.asList(arr);
		}
		// Handle primitive arrays
		int length = java.lang.reflect.Array.getLength(array);
		List<Object> result = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			result.add(java.lang.reflect.Array.get(array, i));
		}
		return result;
	}

	private void storeValue(String key, Object value, ValueContainer container) {
		if (value instanceof AnnotationMirror nestedMirror) {
			// Handle nested annotation by recursing with a path prefix
			extractAnnotationValues(nestedMirror, container, key + "/");
		} else if (value instanceof Map<?, ?> map) {
			// Store maps directly
			container.putValue(key, new HashMap<>(map));
		} else {
			// Store simple values, lists, and arrays directly
			container.putValue(key, value);
		}
	}

	protected @Nullable AnnotationMirror getAnnotationMirror(@NotNull Element element, @NotNull final String annotationClassName) {
		return element.getAnnotationMirrors().stream()
				.filter(m -> m.getAnnotationType().toString().equals(annotationClassName))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Create a typed annotation value extractor for a specific type.
	 * @param type The class of the value type to extract
	 * @param key The key in the annotation to extract
	 * @return A typed value extractor
	 */
	public <T> TypedValueExtractor<T> forType(Class<T> type, String key) {
		return new TypedValueExtractor<>(type, key, this);
	}

	/**
	 * Creates a list value extractor for elements of a specific type.
	 *
	 * @param elementType The class of the list elements
	 * @param key         The key in the annotation to extract
	 * @return A typed list value extractor
	 */
	public <T> ListValueExtractor<T> forList(Class<T> elementType, String key) {
		return new ListValueExtractor<>(elementType, key, this);
	}

	/**
	 * Creates a map value extractor for a specific key/value type combination.
	 *
	 * @param keyType   The class of map keys
	 * @param valueType The class of map values
	 * @param key       The key in the annotation to extract
	 * @return A typed map value extractor
	 */
	public <K, V> MapValueExtractor<K, V> forMap(Class<K> keyType, Class<V> valueType, String key) {
		return new MapValueExtractor<>(keyType, valueType, key, this);
	}

	/**
	 * Gets a type-safe value from the annotation.
	 *
	 * @param key  The key to extract
	 * @param type The expected type
	 * @return Optional containing the value if found and of correct type
	 */
	public <T> Optional<T> getValue(String key, Class<T> type) {
		return extract().getValue(key, type);
	}

	/**
	 * Gets a type-safe list from the annotation.
	 *
	 * @param key         The key to extract
	 * @param elementType The type of elements in the list
	 * @return Optional containing the list if found and elements match type
	 */
	public <T> Optional<List<T>> getList(String key, Class<T> elementType) {
		return extract().getList(key, elementType);
	}

	/**
	 * Gets a type-safe map from the annotation.
	 *
	 * @param key       The key to extract
	 * @param keyType   The type of keys in the map
	 * @param valueType The type of values in the map
	 * @return Optional containing the map if found with matching types
	 */
	public <K, V> Optional<Map<K, V>> getMap(String key, Class<K> keyType, Class<V> valueType) {
		return extract().getMap(key, keyType, valueType);
	}
}
