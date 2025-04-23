package eu.nerdfactor.restness.data;

/**
 * Exception thrown during entity merging operations when errors occur.
 * <p>
 * This exception is thrown by the {@link RestnessEntityMerger} when it encounters
 * problems during the merging process, particularly when trying to merge
 * field values using reflection. Common causes include:
 * - Missing getter or setter methods
 * - Access restrictions to methods
 * - Type incompatibility between fields
 * - Underlying exceptions during method invocation
 *
 * @author Daniel Klug
 * @see RestnessEntityMerger
 */
public class EntityMergerException extends RuntimeException {

	/**
	 * Constructs a new entity merger exception with the specified detail message.
	 * The cause is not initialized and may be later initialized by a call to {@link #initCause}.
	 *
	 * @param message the detail message explaining the error condition
	 */
	public EntityMergerException(String message) {
		super(message);
	}

	/**
	 * Constructs a new entity merger exception with the specified detail message and cause.
	 * This constructor preserves the full call stack of the original exception.
	 *
	 * @param message the detail message explaining the error condition
	 * @param cause   the underlying cause of this exception (typically an exception
	 *                caught during reflection operations)
	 */
	public EntityMergerException(String message, Throwable cause) {
		super(message, cause);
	}
}