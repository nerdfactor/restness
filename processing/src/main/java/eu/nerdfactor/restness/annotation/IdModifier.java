package eu.nerdfactor.restness.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate a method as modifier to set the id
 * of an entity.
 *
 * @author Daniel Klug
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface IdModifier {

	/**
	 * The name of the entity field.
	 */
	String name() default "id";
}
