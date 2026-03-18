package eu.nerdfactor.restness.processing;

import eu.nerdfactor.restness.annotation.RestnessSecurity;
import eu.nerdfactor.restness.config.SecurityConfiguration;
import eu.nerdfactor.restness.processing.extractor.AnnotationValueExtractor;
import eu.nerdfactor.restness.processing.extractor.ValueContainer;
import eu.nerdfactor.restness.util.RestnessUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

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

	public static SecurityConfigurationFromAnnotationBuilder create() {
		return new SecurityConfigurationFromAnnotationBuilder();
	}

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

	/**
	 * Collect information about the security from the annotated class.
	 *
	 * @return {@link SecurityConfiguration} with the found information.
	 */
	public SecurityConfiguration build() {
		// Create parts of the annotated class name.
		String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
		String className = element.getSimpleName().toString();

		ValueContainer annotatedValues = new AnnotationValueExtractor()
				.forClass(RestnessSecurity.class)
				.withElement(element)
				.withUtils(elementUtils)
				.extract();


		// Combine the generated class name and package.
		return new SecurityConfiguration(
				RestnessUtil.resolveGeneratedClassName(
						annotatedValues.getStringOrDefault("className", ""),
						this.classNamePattern,
						this.classNamePrefix,
						className,
						packageName
				),
				annotatedValues.getStringOrDefault("pattern", "{NAME}"),
				annotatedValues.getStringOrDefault("inclusive", "true").equals("true")
		);
	}
}
