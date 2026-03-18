package eu.nerdfactor.restness.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A simplistic entity merger that will try to merge {@link PersistentEntity} or
 * use reflection of getter and setters.
 *
 * @author Daniel Klug
 */
public class RestnessEntityMerger implements DataMerger {

	/**
	 * Update an object by merging it with an updated version.
	 *
	 * @param original The original object.
	 * @param updated  The object with updated values.
	 * @param <T>      Type of the updated object.
	 * @return The original object with the updated values.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T merge(T original, T updated) {
		if (original == null) {
			return updated;
		}
		if (updated == null) {
			return original;
		}
		if (original instanceof PersistentEntity<?> entity) {
			return (T) entity.mergeWithEntity((PersistentEntity<?>) updated);
		}
		return this.reflectMerge(original, updated);
	}

	/**
	 * Merges two objects of the same type by accessing getters and setters.
	 *
	 * @param original The original object.
	 * @param updated  The object with updated values.
	 * @param <T>      Type of the merged objects.
	 * @return The original object with the merged values.
	 */
	protected <T> T reflectMerge(T original, T updated) {
		return this.reflectMerge(original, updated, true);
	}

	/**
	 * Merges two objects of the same type by accessing getters and setters.
	 *
	 * @param original     The original object.
	 * @param updated      The object with updated values.
	 * @param failSilently If true, the method will not throw an exception on failure.
	 *                     Silent Failure may be preferred in a fire and forget merging scenario for
	 *                     values that are known to not be mergeable with this simple implementation.
	 * @param <T>          Type of the merged objects.
	 * @return The original object with the merged values.
	 * @throws EntityMergerException If an error occurs during the merging process.
	 */
	protected <T> T reflectMerge(T original, T updated, boolean failSilently) throws EntityMergerException {
		Method[] methods = original.getClass().getDeclaredMethods();
		for (Method m : methods) {
			String name = m.getName();
			try {
				this.mergeFieldValueByReflection(original, updated, m, name);
			} catch (Exception e) {
				if (!failSilently) {
					throw new EntityMergerException("Error merging Entity because of field: " + name, e);
				}
			}
		}
		return original;
	}

	/**
	 * Merge a single field value from the updated object to the original object using reflection.
	 * This method works by:
	 * 1. Identifying getter methods in the updated object
	 * 2. Retrieving the corresponding value
	 * 3. Finding the matching setter method in the original object
	 * 4. Setting the value in the original object
	 * <p>
	 * The method transfers:
	 * - Non-empty strings
	 * - Primitive types
	 * - Wrapper types (Integer, Long, Double, etc.)
	 *
	 * @param original        The target object to be updated
	 * @param updated         The source object containing updated values
	 * @param reflectedMethod The current method being examined (potential getter)
	 * @param methodName      The name of the current method
	 * @param <T>             The type of both objects
	 * @throws IllegalAccessException    If reflection access fails
	 * @throws InvocationTargetException If the invoked method throws an exception
	 * @throws NoSuchMethodException     If the corresponding setter method is not found
	 */
	protected <T> void mergeFieldValueByReflection(T original, T updated, Method reflectedMethod, String methodName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		String setter = methodName;
		Class<?> type = reflectedMethod.getReturnType();
		Object value = null;
		if (methodName.startsWith("get") && reflectedMethod.getParameterCount() == 0) {
			value = reflectedMethod.invoke(updated);
			setter = methodName.replace("get", "set");
		}
		if (methodName.startsWith("is") && reflectedMethod.getParameterCount() == 0) {
			// getters for booleans may start with "is", per java code conventions.
			value = reflectedMethod.invoke(updated);
			setter = methodName.replace("is", "set");
		}
		if (value != null && ((type == String.class && !value.equals(""))
				|| type.isPrimitive() && !type.equals(Void.TYPE)
				|| Number.class.isAssignableFrom(type)
				|| Boolean.class.equals(type)
				|| Character.class.equals(type))) {
			// Merge non-empty strings, primitive types, and wrapper types
			Method method = original.getClass().getMethod(setter, type);
			method.invoke(original, value);
		}
	}
}
