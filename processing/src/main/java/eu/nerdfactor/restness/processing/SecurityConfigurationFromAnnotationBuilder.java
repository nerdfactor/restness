package eu.nerdfactor.restness.processing;

import eu.nerdfactor.restness.annotation.RestnessSecurity;
import eu.nerdfactor.restness.config.SecurityConfiguration;
import eu.nerdfactor.restness.util.RestnessUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Map;

/**
 * Builder that creates a new security configuration from annotations.
 *
 * @author Daniel Klug
 */
public class SecurityConfigurationFromAnnotationBuilder {

	/**
	 * The annotation processing environment round.
	 */
	protected RoundEnvironment environment;

	/**
	 * The element utilities during annotation processing.
	 */
	protected Elements elementUtils;

	/**
	 * The annotated element.
	 */
	protected TypeElement element;

	/**
	 * The prefix used during generating the class name.
	 */
	protected String classNamePrefix;

	/**
	 * The pattern used to generate the class name.
	 */
	protected String classNamePattern;

	/**
	 * @param env The annotation processing environment round.
	 * @return The builder in a fluent api pattern.
	 */
	public SecurityConfigurationFromAnnotationBuilder withEnvironment(@NotNull RoundEnvironment env) {
		this.environment = env;
		return this;
	}

	/**
	 * @param utils The element utilities during annotation processing.
	 * @return The builder in a fluent api pattern.
	 */
	public SecurityConfigurationFromAnnotationBuilder withUtils(@NotNull Elements utils) {
		this.elementUtils = utils;
		return this;
	}

	/**
	 * @param element The annotated Element.
	 * @return The builder in a fluent api pattern.
	 */
	public SecurityConfigurationFromAnnotationBuilder withElement(@NotNull Element element) {
		this.element = (TypeElement) element;
		return this;
	}

	/**
	 * @param prefix The prefix used during generating the class name.
	 * @return The builder in a fluent api pattern.
	 */
	public SecurityConfigurationFromAnnotationBuilder withPrefix(@NotNull String prefix) {
		this.classNamePrefix = prefix;
		return this;
	}

	/**
	 * @param pattern The pattern used to generate the class name.
	 * @return The builder in a fluent api pattern.
	 */
	public SecurityConfigurationFromAnnotationBuilder withPattern(@NotNull String pattern) {
		this.classNamePattern = pattern;
		return this;
	}

	public static SecurityConfigurationFromAnnotationBuilder create() {
		return new SecurityConfigurationFromAnnotationBuilder();
	}

	/**
	 * Collect information about the security from the annotated class.
	 *
	 * @return {@link SecurityConfiguration} with the found information.
	 */
	public SecurityConfiguration build() {
		// Create parts of the annotated class name.
		String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
		String className = element.getSimpleName().toString();

		// Find all the annotated values in the annotation
		Map<String, String> annotatedValues = new AnnotationValueExtractor()
				.forClass(RestnessSecurity.class)
				.withElement(element)
				.withUtils(elementUtils)
				.extract()
				.values();

		// Combine the generated class name and package.
		String generatedClassName = annotatedValues.getOrDefault("className", "");
		if (generatedClassName.isEmpty()) {
			// todo: remove duplicate code with ControllerConfigurationCollector
			generatedClassName = this.classNamePattern
					.replace("{PREFIX}", this.classNamePrefix)
					.replace("{NAME}", className)
					.replace("{NAME_NORMALIZED}", className.replace("Controller", ""));
		}
		if (!generatedClassName.contains(".")) {
			generatedClassName = packageName + "." + generatedClassName;
		}

		return new SecurityConfiguration(
				RestnessUtil.toClassName(generatedClassName),
				annotatedValues.getOrDefault("pattern", "{NAME}"),
				annotatedValues.getOrDefault("inclusive", "true").equals("true")
		);
	}
}
