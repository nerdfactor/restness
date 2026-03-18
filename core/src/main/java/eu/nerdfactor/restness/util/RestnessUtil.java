package eu.nerdfactor.restness.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.restness.config.AccessorType;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Arrays;
import java.util.List;

/**
 * Utility methods for generated rest.
 *
 * @author Daniel Klug
 */
public class RestnessUtil {

	/**
	 * Simple methods to normalize an entity name by removing common suffix
	 * like Model, Entity, BO or Dao.
	 *
	 * @param name The original entity name.
	 * @return The normalized entity name.
	 */
	public static String normalizeEntityName(@NotNull String name) {
		for (String suffix : Arrays.asList("Model", "Entity", "BO", "Dao")) {
			if (name.endsWith(suffix)) {
				name = name.substring(0, name.length() - suffix.length());
			}
		}
		return name;
	}

	/**
	 * Removes a string from the end of a string.
	 *
	 * @param str    The string.
	 * @param remove The part to remove from the end.
	 * @return The string without the removed part.
	 */
	public static String removeEnd(@NotNull String str, @NotNull String remove) {
		if (!remove.isEmpty() && str.endsWith(remove)) {
			return str.substring(0, str.length() - remove.length());
		}
		return str;
	}

	/**
	 * Turn a TypeName into a ClassName.
	 *
	 * @param typeName The original TypeName.
	 * @return The converted ClassName.
	 */
	public static ClassName toClassName(TypeName typeName) {
		return toClassName(typeName.toString());
	}

	/**
	 * Turns a canonical name of a type into a ClassName.
	 *
	 * @param typeName The canonical name of a class.
	 * @return The converted ClassName.
	 */
	public static ClassName toClassName(String typeName) {
		return toClassName(typeName, "");
	}

	/**
	 * Turns a canonical name of a type into a ClassName and
	 * adds a new prefix to the class.
	 *
	 * @param typeName The canonical name of the class.
	 * @param prefix   The new prefix for the class.
	 * @return The converted ClassName.
	 */
	public static ClassName toClassName(String typeName, String prefix) {
		String className = typeName.substring(typeName.lastIndexOf('.') + 1).trim();
		String packageName = removeEnd(typeName, "." + className);
		return ClassName.get(packageName, prefix + className);
	}

	/**
	 * Get the method name for the given {@link AccessorType}. Trys to
	 * singularize the relation name and adds the prefix for the method name.
	 *
	 * @param type The {@link AccessorType} to get the method name for.
	 * @return The method name for the given {@link AccessorType}.
	 **/
	public static String getRelationMethodName(String relationName, AccessorType type) {
		String methodName = relationName.substring(0, 1).toUpperCase() + relationName.substring(1);
		String singularName = WordInflector.getInstance().singularize(methodName);
		return switch (type) {
			case GET -> "get" + methodName;
			case SET -> "set" + methodName;
			case ADD -> "add" + singularName;
			case REMOVE -> "remove" + singularName;
		};
	}

	/**
	 * Resolves a generated class name from configuration values, applying prefix
	 * and pattern substitution if no explicit class name is provided.
	 *
	 * @param configuredClassName The explicitly configured class name (may be empty).
	 * @param classNamePattern    The pattern for generating the class name (e.g. "{PREFIX}{NAME}").
	 * @param classNamePrefix     The prefix to substitute into the pattern (e.g. "Generated").
	 * @param className           The simple name of the annotated class.
	 * @param packageName         The package name of the annotated class.
	 * @return The resolved {@link ClassName}.
	 */
	public static ClassName resolveGeneratedClassName(@NotNull String configuredClassName, @NotNull String classNamePattern, @NotNull String classNamePrefix, @NotNull String className, @NotNull String packageName) {
		String generatedClassName = configuredClassName;
		if (generatedClassName.isEmpty()) {
			generatedClassName = classNamePattern
					.replace("{PREFIX}", classNamePrefix)
					.replace("{NAME}", className)
					.replace("{NAME_NORMALIZED}", className.replace("Controller", ""));
		}
		if (!generatedClassName.contains(".")) {
			generatedClassName = packageName + "." + generatedClassName;
		}
		return RestnessUtil.toClassName(generatedClassName);
	}

	/**
	 * Check if there already exists a request mapping for the given path and methods.
	 *
	 * @param existingRequestMappings The list of existing request mappings.
	 * @param path                    The path to check.
	 * @param methods                 The methods to check.
	 * @return True if there is an existing request mapping for the given path and methods.
	 */
	public static boolean hasExistingRequest(List<String> existingRequestMappings, String path, RequestMethod... methods) {
		return hasExistingRequest(existingRequestMappings, path, Arrays.stream(methods).map(RequestMethod::name).toArray(String[]::new));
	}

	/**
	 * Check if there already exists a request mapping for the given path and methods.
	 *
	 * @param existingRequestMappings The list of existing request mappings.
	 * @param path                    The path to check.
	 * @param methods                 The methods to check.
	 * @return True if there is an existing request mapping for the given path and methods.
	 */
	public static boolean hasExistingRequest(List<String> existingRequestMappings, String path, String... methods) {
		if (methods == null || methods.length == 0) {
			return false;
		}

		String lowerPath = path.toLowerCase();
		for (String method : methods) {
			if (existingRequestMappings.contains(method.toUpperCase() + lowerPath)) {
				return true;
			}
		}
		return false;
	}
}
