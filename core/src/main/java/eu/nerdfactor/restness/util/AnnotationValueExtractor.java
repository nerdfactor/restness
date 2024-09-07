package eu.nerdfactor.restness.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
public class AnnotationValueExtractor {

	/**
	 * The canonical name (with full package) of the annotation class
	 * that will be checked during extraction.
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

	public ValueWrapper extract() {
		return this.extract(new ValueWrapper(this.element, this.className));
	}

	public List<ValueWrapper> extractList() {
		return this.extractList(new ArrayList<>());
	}

	public List<ValueWrapper> extractList(List<ValueWrapper> values) {
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

	public ValueWrapper extract(ValueWrapper values) {
		this.extractInto(values.getValues());
		return values;
	}

	public void extractInto(Map<String, String> values) {
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
				} else if (value instanceof AnnotationValue) {
					// todo: handle nested annotations
					addAnnotatedValues((AnnotationMirror) ((AnnotationValue) value).getValue(), values, name + "/");
				} else {
					values.put(name, value.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Get the specified {@link AnnotationMirror} form a {@link Element}.
	 *
	 * @param element             The annotated {@link Element}.
	 * @param annotationClassName The name of the {@link Annotation}.
	 * @return The specified {@link AnnotationMirror} if the element is annotated by the specified {@link Annotation}.
	 */
	protected @Nullable AnnotationMirror getAnnotationMirror(@NotNull Element element, @NotNull final String annotationClassName) {
		return element.getAnnotationMirrors().stream()
				.filter(m -> m.getAnnotationType().toString().equals(annotationClassName))
				.findFirst()
				.orElse(null);
	}

	/**
	 * A simple wrapper class containing the
	 * extracted values, the name of the annotation
	 * and the element, that was annotated.
	 */
	@Getter
	@Setter
	@RequiredArgsConstructor
	@AllArgsConstructor
	public static class ValueWrapper {

		private final Element element;
		private final String annotationClassName;
		private Map<String, String> values = new HashMap<>();
	}

}
