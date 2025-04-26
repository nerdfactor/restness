package eu.nerdfactor.restness.processing.extractor;

import lombok.extern.slf4j.Slf4j;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extracts values from {@link Annotation Annotations} during annotation processing.
 *
 * @author Daniel Klug
 * @deprecated This class is deprecated and should not be used in new code. Use {@link AnnotationValueExtractor} instead.
 */
@Slf4j
@Deprecated
public class UnsafeAnnotationValueExtractor extends AnnotationValueExtractor {

	public UnsafeAnnotationValueExtractor forClass(Class<? extends Annotation> annotationClass) {
		return this.forClass(annotationClass.getCanonicalName());
	}

	public UnsafeAnnotationValueExtractor forClass(String className) {
		return (UnsafeAnnotationValueExtractor) super.forClass(className);
	}

	public UnsafeAnnotationValueExtractor withElement(Element element) {
		return (UnsafeAnnotationValueExtractor) super.withElement(element);
	}

	public UnsafeAnnotationValueExtractor withUtils(Elements utils) {
		return (UnsafeAnnotationValueExtractor) super.withUtils(utils);
	}

	/**
	 * @deprecated Use {@link #extract()} instead which fully supports arrays and nested annotations.
	 */
	@Deprecated
	public ValueWrapper extractUnsafe() {
		return convertToLegacyValueWrapper(extract());
	}

	/**
	 * @deprecated Use {@link #extractList()} instead which fully supports arrays and nested annotations.
	 */
	@Deprecated
	public List<ValueWrapper> extractListUnsafe() {
		return extractList().stream()
				.map(this::convertToLegacyValueWrapper)
				.toList();
	}

	/**
	 * @deprecated Use {@link #extractList()} instead which fully supports arrays and nested annotations.
	 */
	@Deprecated
	public List<ValueWrapper> extractListUnsafe(List<ValueWrapper> values) {
		values.addAll(extractListUnsafe());
		return values;
	}

	/**
	 * @deprecated Use {@link #extractInto(ValueContainer)} instead which fully supports arrays and nested annotations.
	 */
	@Deprecated
	public ValueWrapper extractUnsafe(ValueWrapper wrapper) {
		ValueContainer container = new ValueContainer(wrapper.element(), wrapper.annotationClassName());
		extractInto(container);
		copyLegacyValues(container, wrapper.values());
		return wrapper;
	}

	/**
	 * @deprecated Use {@link #extractInto(ValueContainer)} instead which fully supports arrays and nested annotations.
	 */
	@Deprecated
	public void extractIntoUnsafe(Map<String, String> values) {
		ValueContainer container = extract();
		copyLegacyValues(container, values);
	}

	private ValueWrapper convertToLegacyValueWrapper(ValueContainer container) {
		Map<String, String> stringValues = new HashMap<>();
		copyLegacyValues(container, stringValues);
		return new ValueWrapper(container.getElement(), container.getAnnotationClassName(), stringValues);
	}

	/**
	 * Helper method to copy values from a ValueContainer to a string map in the legacy format.
	 * Skips arrays, lists, maps and nested annotations to maintain backward compatibility.
	 */
	private void copyLegacyValues(ValueContainer container, Map<String, String> target) {
		container.getValues().forEach((key, value) -> {
			if (isLegacyCompatibleValue(value)) {
				target.put(key, convertToLegacyString(value));
			}
		});
	}

	private boolean isLegacyCompatibleValue(Object value) {
		if (value == null) return false;
		return !(value instanceof List<?> ||
				value.getClass().isArray() ||
				value instanceof Map<?, ?> ||
				value instanceof AnnotationMirror);
	}

	private String convertToLegacyString(Object value) {
		if (value == null) return "";

		if (value instanceof String || value instanceof Number || value instanceof Boolean) {
			return String.valueOf(value);
		}

		if (value instanceof Enum<?>) {
			return ((Enum<?>) value).name();
		}

		// For other types, use toString but log a warning
		log.warn("Converting non-standard type {} to string in legacy mode. Consider using the new API for better type safety.",
				value.getClass().getName());
		return value.toString();
	}

	/**
	 * A simple wrapper class containing the extracted values, the name of the
	 * annotation and the element that was annotated.
	 */
	public record ValueWrapper(Element element, String annotationClassName,
	                           Map<String, String> values) {
	}
}
