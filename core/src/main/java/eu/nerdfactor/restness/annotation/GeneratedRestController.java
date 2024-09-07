package eu.nerdfactor.restness.annotation;

import java.lang.annotation.*;

/**
 * Annotation class to generate a new controller class containing REST methods for reading
 * and changing the specified entity.
 * <br>
 * Should be on a controller class in order to reflect basic information like the name.
 * But can be added to any other class. In that case the name is required in order to
 * determine the name of the generated class.
 *
 * @author Daniel Klug
 */
@Repeatable(GeneratedRestController.List.class)
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface GeneratedRestController {

	/**
	 * The path for the request.
	 */
	String value() default "";

	/**
	 * The name of the generated class. By default, it will extend
	 * the name of the annotated class. By default, it will be
	 * in the same package as the annotated class.
	 */
	String className() default "";

	/**
	 * Class of the entity that will be accessed by the generated controller.
	 */
	Class<?> entity();

	/**
	 * Type of the id for the accessed entity.
	 */
	Class<?> id();

	/**
	 * Class of the data transfer objet that may be in the response of the
	 * generated controller. Use the same as the entity to not map to a dto.
	 */
	Class<?> dto() default Object.class;

	/**
	 * Configuration of data transfer object classes.
	 * Will contain the classes for a single dto, a list dto and a request dto.
	 * Those classes will be used if dto() is not set.
	 */
	DtoConfiguration dtoConfig() default @DtoConfiguration(value = Object.class);

	/**
	 * By default, endpoints for all the relations annotated with OneToMany, ManyToOne,
	 * ManyToMany or OneToOne from jpa in the entity will be created. Access to relation
	 * objects will use methods called get{RelationName}, set{RelationName} and/or
	 * add{RelationName} and remove{RelationName}. Otherwise, methods can be marked with
	 * annotations to declare their function (get, set, add, remove).
	 */
	boolean withRelations() default true;

	@Retention(RetentionPolicy.SOURCE)
	@Target({ElementType.TYPE})
	@interface List {
		GeneratedRestController[] value();
	}
}
