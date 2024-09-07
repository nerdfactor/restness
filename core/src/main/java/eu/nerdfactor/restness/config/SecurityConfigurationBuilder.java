package eu.nerdfactor.restness.config;

import eu.nerdfactor.restness.annotation.GeneratedRestSecurity;
import eu.nerdfactor.restness.util.AnnotationValueExtractor;
import eu.nerdfactor.restness.util.GeneratedRestUtil;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Map;

/**
 * Builder that creates a new security configuration from the
 * provided information.
 *
 * @author Daniel Klug
 */
public class SecurityConfigurationBuilder {

	RoundEnvironment environment;
	Elements elementUtils;
	TypeElement element;

	private String classNamePrefix;
	private String classNamePattern;

	/**
	 * @param element The annotated Element.
	 * @return The ControllerConfigurationCollector.
	 */
	public SecurityConfigurationBuilder fromElement(Element element) {
		this.element = (TypeElement) element;
		return this;
	}

	public SecurityConfigurationBuilder withUtils(Elements utils) {
		this.elementUtils = utils;
		return this;
	}

	public SecurityConfigurationBuilder withEnvironment(RoundEnvironment env) {
		this.environment = env;
		return this;
	}

	public SecurityConfigurationBuilder withPrefix(String prefix) {
		this.classNamePrefix = prefix;
		return this;
	}

	public SecurityConfigurationBuilder withPattern(String pattern) {
		this.classNamePattern = pattern;
		return this;
	}

	/**
	 * Collect information about the controller from the annotated class.
	 *
	 * @return ControllerConfiguration with the found information.
	 */
	public SecurityConfiguration build() {
		// Create parts of the annotated class name.
		String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
		String className = element.getSimpleName().toString();

		// Find all the annotated values in the annotation
		Map<String, String> annotatedValues = new AnnotationValueExtractor()
				.forClass(GeneratedRestSecurity.class)
				.withElement(element)
				.withUtils(elementUtils)
				.extract()
				.getValues();

		// Combine the generated class name and package.
		String generatedClassName = annotatedValues.getOrDefault("className", "");
		if (generatedClassName.isEmpty()) {
			// todo: duplicate code with ControllerConfigurationCollector
			generatedClassName = this.classNamePattern
					.replace("{PREFIX}", this.classNamePrefix)
					.replace("{NAME}", className)
					.replace("{NAME_NORMALIZED}", className.replace("Controller", ""));
		}
		if (!generatedClassName.contains(".")) {
			generatedClassName = packageName + "." + generatedClassName;
		}

		return new SecurityConfiguration(
				GeneratedRestUtil.toClassName(generatedClassName),
				annotatedValues.getOrDefault("pattern", "{NAME}"),
				annotatedValues.getOrDefault("inclusive", "true").equals("true")
		);
	}
}
