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
 * Extracts values from {@link Annotation Annotations} during annotation
 * processing.
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
	 * Extracts the values from the annotation and returns them in a
	 * {@link ValueWrapper}.
	 *
	 * @return The extracted values.
	 * @deprecated Replace with typesafe extractList() during refactor.
	 */
	@Deprecated
	public ValueWrapper extractUnsafe() {
		return this.extractUnsafe(new ValueWrapper(this.element, this.className, new HashMap<>()));
	}

	/**
	 * Extracts the values from the annotation and returns them in a
	 * {@link ValueWrapper}.
	 *
	 * @return The extracted values.
	 * @deprecated Replace with typesafe extractList() during refactor.
	 */
	@Deprecated
	public List<ValueWrapper> extractListUnsafe() {
		return this.extractListUnsafe(new ArrayList<>());
	}

	/**
	 * Extracts the values from the annotation and adds them to the given list.
	 *
	 * @param values The list to add the extracted values to.
	 * @return The list with the extracted values.
	 * @deprecated Replace with typesafe extractList(values) during refactor.
	 */
	@Deprecated
	public List<ValueWrapper> extractListUnsafe(List<ValueWrapper> values) {
		final AnnotationMirror annotationMirror = getAnnotationMirror(element, className);

		if (annotationMirror != null) {
			Class<?> c = annotationMirror.getElementValues().values().stream().findFirst().orElseThrow().getValue().getClass();
			if (List.class.isAssignableFrom(c) || Collection.class.isAssignableFrom(c) || c.isArray()) {
				annotationMirror.getElementValues().forEach((executableElement, annotationValue) -> {
					Object valueList = annotationValue.getValue();
					if (valueList instanceof Iterable<?> iter) {
						iter.forEach(mirror -> {
							Map<String, String> val = new HashMap<>();
							this.addAnnotatedValues((AnnotationMirror) mirror, val);
							values.add(new ValueWrapper(this.element, this.className, val));
						});
					}
				});
			} else {
				Map<String, String> val = new HashMap<>();
				this.addAnnotatedValues(annotationMirror, val);
				values.add(new ValueWrapper(this.element, this.className, val));
			}
		}
		return values;
	}

	/**
	 * Extracts the values from the annotation and adds them to the given
	 * {@link ValueWrapper}.
	 *
	 * @param values The {@link ValueWrapper} to add the extracted values to.
	 * @return The {@link ValueWrapper} with the extracted values.
	 * @deprecated Replace with typesafe extract(values) during refactor.
	 */
	@Deprecated
	public ValueWrapper extractUnsafe(ValueWrapper values) {
		this.extractIntoUnsafe(values.values);
		return values;
	}

	/**
	 * Extracts the values from the annotation and adds them to the given map.
	 *
	 * @param values The map to add the extracted values to.
	 * @deprecated Replace with typesafe extractInto(values) during refactor.
	 */
	@Deprecated
	public void extractIntoUnsafe(Map<String, String> values) {
		final AnnotationMirror annotationMirror = getAnnotationMirror(element, className);
		if (annotationMirror != null) {
			this.addAnnotatedValues(annotationMirror, values);
		}
	}

	protected void addAnnotatedValues(AnnotationMirror annotationMirror, Map<String, String> values) {
		this.addAnnotatedValues(annotationMirror, values, "");
	}

	protected void addAnnotatedValues(AnnotationMirror annotationMirror, Map<String, String> values, final String prefix) {
		final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = this.utils.getElementValuesWithDefaults(annotationMirror);
		elementValues.forEach((executableElement, annotationValue) -> {
			try {
				String name = prefix + executableElement.getSimpleName().toString();
				Object value = annotationValue.getValue();

				if (value.getClass().isArray()) {
					// todo: handle arrays
				} else if (value instanceof AnnotationMirror nestedMirror) {
					// Handle nested annotations by recursively extracting their values
					addAnnotatedValues(nestedMirror, values, name + "/");
				} else if (value instanceof AnnotationValue nestedValue) {
					// Handle nested annotation values by recursively extracting their values
					Object innerValue = nestedValue.getValue();
					if (innerValue instanceof AnnotationMirror innerMirror) {
						addAnnotatedValues(innerMirror, values, name + "/");
					} else {
						values.put(name, innerValue.toString());
					}
				} else {
					values.put(name, value.toString());
				}
			} catch (Exception e) {
				log.debug("Error extracting annotation value", e);
			}
		});
	}

	/**
	 * Get the specified {@link AnnotationMirror} form a {@link Element}.
	 *
	 * @param element             The annotated {@link Element}.
	 * @param annotationClassName The name of the {@link Annotation}.
	 * @return The specified {@link AnnotationMirror} if the element is
	 * annotated by the specified {@link Annotation}.
	 */
	protected @Nullable AnnotationMirror getAnnotationMirror(@NotNull Element element, @NotNull final String annotationClassName) {
		return element.getAnnotationMirrors().stream()
				.filter(m -> m.getAnnotationType().toString().equals(annotationClassName))
				.findFirst()
				.orElse(null);
	}

	/**
	 * A simple wrapper class containing the extracted values, the name of the
	 * annotation and the element, that was annotated.
	 */
	public record ValueWrapper(Element element, String annotationClassName,
	                           Map<String, String> values) {

	}

}
