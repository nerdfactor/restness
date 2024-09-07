package eu.nerdfactor.restness.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate a method as accessor to get the id
 * of an entity.
 *
 * @author Daniel Klug
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface IdAccessor {

	/**
	 * The name of the entity field.
	 */
	String name() default "id";
}
