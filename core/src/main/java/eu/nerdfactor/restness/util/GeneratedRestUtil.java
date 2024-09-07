package eu.nerdfactor.restness.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Utility methods for generated rest.
 *
 * @author Daniel Klug
 */
public class GeneratedRestUtil {

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

	public static boolean LOG = false;

	/**
	 * Simplistic log helper.
	 *
	 * @param str The log string.
	 */
	public static void log(String str) {
		log(str, 0);
	}

	public static void log(String str, int indentation) {
		if (LOG) {
			System.out.println("[INFO] " + "  ".repeat(indentation) + str);
		}
	}
}
