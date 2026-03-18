package eu.nerdfactor.restness.annotation;

import eu.nerdfactor.restness.generate.JavaClassGenerator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation class to configure the generation of generated rest controllers.
 *
 * @author Daniel Klug
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RestnessConfiguration {

	/**
	 * Indentation with four spaces.
	 */
	String INDENT_SPACE = "    ";

	/**
	 * Indentation with a tab.
	 */
	String INDENT_TAB = "\t";

	/**
	 * The style of indentation for generated classes.
	 */
	String indentation() default INDENT_TAB;

	/**
	 * Prefix that will be used to generated controller class names.
	 */
	String classNamePrefix() default "Generated";

	/**
	 * Pattern to create generated controller class names if the name is not
	 * specifically provided.
	 * <li>Prefix: Value of classNamePrefix.</li>
	 * <li>NAME: Name of the class.</li>
	 * <li>NAME_NORMALIZED: Name of class without Controller suffix.</li>
	 */
	String classNamePattern() default "{PREFIX}{NAME}";

	/**
	 * Alternative to ResponseEntity in order ot attach additional
	 * meta information to the response (i.e. api version).
	 */
	Class<?> dataWrapper() default Object.class;

	/**
	 * Namespace for Dto Classes. This will be used to restrict discovery of Dto classes to this
	 * namespace.
	 */
	String dtoNamespace() default "";

	/**
	 * Class used to generate the classes.
	 */
	Class<?> generator() default JavaClassGenerator.class;

	/**
	 * Class that will be used for optional exporting of the configuration.
	 */
	Class<?> exporter() default Object.class;

	/**
	 * Path to the export file. This will be used to export the configuration
	 * if an exporter is provided.
	 */
	String exportPath() default "";

	/**
	 * Class that will be used for optional importing of the configuration.
	 * If this class is provided, the imported configuration will be used
	 * instead of the discovered configuration.
	 */
	Class<?> importer() default Object.class;

	/**
	 * Path to the import file. This will be used to import the configuration
	 * if an importer is provided.
	 */
	String importPath() default "";

	/**
	 * Enable generation of OpenAPI/Swagger annotations on endpoints.
	 * When enabled, generated controllers will include @Tag, @Operation,
	 * and @ApiResponse annotations from io.swagger.v3.oas.annotations.
	 * Consumer projects must have swagger-annotations (2.2.0+) on their classpath.
	 */
	boolean openApi() default false;
}
