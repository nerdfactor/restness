package eu.nerdfactor.restness.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;

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
		if (original.getClass().isRecord()) {
			return this.recordMerge(original, updated);
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
	 * Merges two Records of the same type by reading component values and
	 * constructing a new instance via the canonical constructor.
	 *
	 * @param original The original Record.
	 * @param updated  The Record with updated values.
	 * @param <T>      Type of the merged Records.
	 * @return A new Record instance with the merged values.
	 */
	protected <T> T recordMerge(T original, T updated) {
		return this.recordMerge(original, updated, true);
	}

	/**
	 * Merges two Records of the same type by reading component values and
	 * constructing a new instance via the canonical constructor.
	 * <p>
	 * For each component, the updated value is used if it passes the
	 * {@link #shouldMergeValue(Object, Class)} check; otherwise the
	 * original value is preserved. This means primitive default values
	 * (0, false, '\0') from the updated Record will always overwrite
	 * the original — consistent with POJO reflection merge behavior.
	 *
	 * @param original     The original Record.
	 * @param updated      The Record with updated values.
	 * @param failSilently If true, errors are swallowed and the original is returned.
	 * @param <T>          Type of the merged Records.
	 * @return A new Record instance with the merged values.
	 * @throws EntityMergerException If an error occurs and failSilently is false.
	 */
	@SuppressWarnings("unchecked")
	protected <T> T recordMerge(T original, T updated, boolean failSilently) throws EntityMergerException {
		Class<?> clazz = original.getClass();
		RecordComponent[] components = clazz.getRecordComponents();
		Object[] args = new Object[components.length];
		Class<?>[] types = new Class<?>[components.length];

		for (int i = 0; i < components.length; i++) {
			RecordComponent component = components[i];
			types[i] = component.getType();
			try {
				Method accessor = component.getAccessor();
				Object originalValue = accessor.invoke(original);
				Object updatedValue = accessor.invoke(updated);
				args[i] = shouldMergeValue(updatedValue, types[i]) ? updatedValue : originalValue;
			} catch (Exception e) {
				if (!failSilently) {
					throw new EntityMergerException("Error merging Record component: " + component.getName(), e);
				}
				try {
					args[i] = component.getAccessor().invoke(original);
				} catch (Exception ex) {
					args[i] = getDefaultValue(types[i]);
				}
			}
		}

		try {
			Constructor<?> constructor = clazz.getDeclaredConstructor(types);
			return (T) constructor.newInstance(args);
		} catch (Exception e) {
			if (!failSilently) {
				throw new EntityMergerException("Error constructing merged Record: " + clazz.getName(), e);
			}
			return original;
		}
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
		if (shouldMergeValue(value, type)) {
			Method method = original.getClass().getMethod(setter, type);
			method.invoke(original, value);
		}
	}

	/**
	 * Checks whether a value should be merged based on its type.
	 * Mergeable types are: non-empty Strings, primitives (except void),
	 * Number subclasses, Boolean, and Character.
	 *
	 * @param value The value to check.
	 * @param type  The type of the value.
	 * @return True if the value should be merged.
	 */
	protected boolean shouldMergeValue(Object value, Class<?> type) {
		return value != null && ((type == String.class && !value.equals(""))
				|| type.isPrimitive() && !type.equals(Void.TYPE)
				|| Number.class.isAssignableFrom(type)
				|| Boolean.class.equals(type)
				|| Character.class.equals(type));
	}

	/**
	 * Returns the default value for a given type. Used as a last resort
	 * fallback when Record component values cannot be read.
	 *
	 * @param type The type to get a default value for.
	 * @return The default value (0 for numeric, false for boolean, '\0' for char, null for reference types).
	 */
	private Object getDefaultValue(Class<?> type) {
		if (!type.isPrimitive()) return null;
		if (type == boolean.class) return false;
		if (type == char.class) return '\0';
		if (type == byte.class) return (byte) 0;
		if (type == short.class) return (short) 0;
		if (type == int.class) return 0;
		if (type == long.class) return 0L;
		if (type == float.class) return 0.0f;
		if (type == double.class) return 0.0d;
		return null;
	}
}
