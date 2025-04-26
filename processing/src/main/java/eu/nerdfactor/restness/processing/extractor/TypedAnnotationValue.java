package eu.nerdfactor.restness.processing.extractor;

/**
 * Interface for an annotation value that can be converted to a specific type.
 */
public interface TypedAnnotationValue<T> {
	T getValue();

	String getKey();
}
