package eu.nerdfactor.restness.annotation;

import eu.nerdfactor.restness.config.RelationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate a relation a field with a relation to a
 * different entity.
 *
 * @author Daniel Klug
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Relation {

	/**
	 * By default, the name of the field will be used.
	 */
	String name() default "";

	/**
	 * Differentiate between a relation to one single object and a collection of
	 * objects. By default, the type of the field will be reflected to determine
	 * if is a collection.
	 */
	RelationType type() default RelationType.REFLECT;

	/**
	 * By default, the method get{RelationName} will be accessed to collect
	 * the relation object.
	 */
	String get() default "";

	/**
	 * By default, the method set{RelationName} will be accessed to set
	 * the relation object. For single relations the same method will
	 * be used to remove objects by setting them to null.
	 */
	String set() default "";

	/**
	 * By default, the method add{RelationName} will be accessed to add
	 * a relation object to the collection.
	 */
	String add() default "";

	/**
	 * By default, the method remove{RelationName} will be accessed to remove
	 * a relation object from the collection.
	 */
	String remove() default "";

	/**
	 * Full name of the entity class. By default, the type of the field will
	 * be used.
	 */
	String entity() default "";

	/**
	 * Full name of the dto class. By default, the type will be determined from
	 * the entity name.
	 */
	String dto() default "";
}
