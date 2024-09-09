package eu.nerdfactor.restness.annotation;

import java.lang.annotation.*;

/**
 * Annotation class to add Spring security Annotations to generated controller classes.
 * <br>
 * Should be on a controller class in order to reflect basic informationen like the name.
 * But can be added to any other class. In that case the name is required in order to
 * determine the name of the controller class.
 *
 * @author Daniel Klug
 */
@Repeatable(RestnessSecurity.List.class)
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RestnessSecurity {


	/**
	 * The name of the generated class. By default, it will extend
	 * the name of the annotated class. By default, it will be
	 * in the same package as the annotated class. Will be used to match the
	 * security do a generated controller.
	 */
	String className() default "";

	/**
	 * Pattern for roles that will be checked on rest methods.
	 * <li>METHOD: Type of method - CREATE, READ, UPDATE, DELETE</li>
	 * <li>ENTITY: Name of the entity.</li>
	 * <li>Name: Name of relation or entity.</li>
	 */
	String pattern() default "ROLE_{METHOD}_{ENTITY}";

	/**
	 * Include roles for base entity of relations.
	 * <br>
	 * <li>If true: Adding an Order to Customer will require access to Order and Customer.</li>
	 * <li>If false: Adding an Order to Customer will only require access to Order.</li>
	 */
	boolean inclusive() default true;

	@Retention(RetentionPolicy.SOURCE)
	@Target({ElementType.TYPE})
	@interface List {
		RestnessSecurity[] value();
	}

}
