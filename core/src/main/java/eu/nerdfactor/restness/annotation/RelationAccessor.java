package eu.nerdfactor.restness.annotation;

import eu.nerdfactor.restness.config.AccessorType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate a method as accessor for relations
 * with other entities.
 *
 * @author Daniel Klug
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface RelationAccessor {

	/**
	 * The name of the relation that was determined by a
	 * Relation annotation or an annotation from Spring JPA.
	 */
	String name();

	/**
	 * One or multiple types of access that can be provided
	 * by the method.
	 * <br>
	 * <li>GET: get relation object.</li>
	 * <li>SET: set relation object.</li>
	 * <li>ADD: add a relation object.</li>
	 * <li>REMOVE: remove a relation object.</li>
	 *
	 * @see AccessorType
	 */
	AccessorType[] type() default {};
}
