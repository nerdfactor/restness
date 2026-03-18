package eu.nerdfactor.restness.processing;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.restness.annotation.RestnessConfiguration;
import eu.nerdfactor.restness.annotation.RestnessController;
import eu.nerdfactor.restness.annotation.RestnessSecurity;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.config.SecurityConfiguration;
import eu.nerdfactor.restness.generate.JavaClassGenerator;
import eu.nerdfactor.restness.generate.RestnessGenerator;
import eu.nerdfactor.restness.processing.extractor.AnnotationValueExtractor;
import eu.nerdfactor.restness.processing.extractor.ValueContainer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.*;

/**
 * Annotation processor for generated rest controllers. Will check all
 * {@link RestnessController}, {@link RestnessSecurity} and
 * {@link RestnessConfiguration} annotations and build new controller classes
 * out of the information.
 * <p>
 * <a href="https://stackoverflow.com/a/31358366">How to debug</a>
 * In directory of pom: mvnDebug clean test
 *
 * @author Daniel Klug
 */
@Slf4j
@SupportedAnnotationTypes({
		"eu.nerdfactor.restness.annotation.RestnessController",
		"eu.nerdfactor.restness.annotation.RestnessSecurity",
		"eu.nerdfactor.restness.annotation.RestnessConfiguration"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class RestnessAnnotationProcessor extends AbstractProcessor {

	private Filer filer;
	private Elements elementUtils;
	private int rounds = 1;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnvironment) {
		super.init(processingEnvironment);
		this.filer = processingEnvironment.getFiler();
		this.elementUtils = processingEnvironment.getElementUtils();
		log.info("RestnessAnnotationProcessor initialized");
	}

	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
		log.debug("Processing round {} started.", this.rounds);
		final Map<String, ControllerConfiguration> controllers = new HashMap<>();

		// Get all values from DynamicRestConfiguration annotations into one map.
		final Map<String, String> generatedConfig = new HashMap<>();
		for (Element element : roundEnvironment.getElementsAnnotatedWith(RestnessConfiguration.class)) {
			if (element.getKind() != ElementKind.CLASS) {
				log.debug("RestnessConfiguration annotation is only supported on classes. Ending round {}.", this.rounds);
				return true;
			}

			ValueContainer annotatedValues = new AnnotationValueExtractor()
					.withUtils(this.elementUtils)
					.withElement(element)
					.forClass(RestnessConfiguration.class)
					.extract();

			generatedConfig.putAll(annotatedValues.getStringValues());

			log.info("GeneratedConfig");
			generatedConfig.forEach((name, value) -> {
				log.info("{}: {}", name, value);
			});
		}

		boolean importedConfiguration = false;
		String importerClassName = generatedConfig.getOrDefault("importer", null);
		String importPath = generatedConfig.getOrDefault("importPath", "");
		if (importerClassName != null && !importerClassName.isEmpty() && !importPath.isEmpty()) {
			// todo: Import the configuration from a file.
			importedConfiguration = true;
		}

		if (!importedConfiguration) {
			// Get all DynamicRestController annotations and gather information from the specified
			// entity in order to create a ControllerConfiguration.
			this.findControllerValueContainer(roundEnvironment).forEach(container -> {
				ControllerConfiguration config = ControllerConfigurationFromAnnotationBuilder.create()
						.withElement(container.getElement())
						.withUtils(this.elementUtils)
						.withEnvironment(roundEnvironment)
						.withAnnotatedValues(container.getStringValues())
						.withPrefix(generatedConfig.getOrDefault("classNamePrefix", "Generated"))
						.withPattern(generatedConfig.getOrDefault("classNamePattern", "{PREFIX}{NAME}"))
						.withResponseWrapper(ClassName.bestGuess(generatedConfig.getOrDefault("dataWrapper", Object.class.getCanonicalName())))
						.withDtoClasses(this.findDtoClasses(roundEnvironment, generatedConfig.getOrDefault("dtoNamespace", "")))
						.build();
				controllers.put(config.getControllerClassName().simpleName(), config);
			});

			// Get all DynamicRestSecurity annotations and add them to the matching controllers.
			for (Element element : roundEnvironment.getElementsAnnotatedWith(RestnessSecurity.class)) {
				if (element.getKind() != ElementKind.CLASS) {
					log.debug("RestnessSecurity annotation is only supported on classes. Ending round {}.", this.rounds);
					return true;
				}
				SecurityConfiguration security = SecurityConfigurationFromAnnotationBuilder.create()
						.withElement(element)
						.withUtils(this.elementUtils)
						.withEnvironment(roundEnvironment)
						.withPrefix(generatedConfig.getOrDefault("classNamePrefix", "Generated"))
						.withPattern(generatedConfig.getOrDefault("classNamePattern", "{PREFIX}{NAME}"))
						.build();
				if (controllers.containsKey(security.getControllerClassName().simpleName())) {
					controllers.get(security.getControllerClassName().simpleName()).setSecurityConfig(security);
				}
			}
		}

		String exporterClassName = generatedConfig.getOrDefault("exporter", null);
		String exportPath = generatedConfig.getOrDefault("exportPath", "");
		if (exporterClassName != null && !exporterClassName.isEmpty() && !exportPath.isEmpty()) {
			// todo: Export the configuration to a file.

		}

		// Take the ControllerConfigurations and build new classes from them.
		String generatorClassName = generatedConfig.getOrDefault("generator", JavaClassGenerator.class.getCanonicalName());
		try {
			// todo: maybe a factory is better?
			Class<? extends RestnessGenerator> cls = Class.forName(generatorClassName).asSubclass(RestnessGenerator.class);
			RestnessGenerator generator = cls.getDeclaredConstructor().newInstance();
			generator.withFiler(this.filer).generate(generatedConfig, controllers);
		} catch (Exception e) {
			log.error("Failed to instantiate or run generator {}.", generatorClassName, e);
			e.printStackTrace();
		}

		log.debug("Processing round {} ended.", this.rounds);
		this.rounds++;
		return true;
	}

	private List<ValueContainer> findControllerValueContainer(RoundEnvironment roundEnvironment) {
		List<ValueContainer> controllerValues = new ArrayList<>();
		for (Element element : roundEnvironment.getElementsAnnotatedWith(RestnessController.List.class)) {
			controllerValues.add(new AnnotationValueExtractor()
					.forClass(RestnessController.List.class)
					.withElement(element)
					.withUtils(this.elementUtils)
					.extract());
		}
		for (Element element : roundEnvironment.getElementsAnnotatedWith(RestnessController.class)) {
			controllerValues.add(new AnnotationValueExtractor()
					.forClass(RestnessController.class)
					.withElement(element)
					.withUtils(this.elementUtils)
					.extract());
		}
		return controllerValues;
	}

	/**
	 * Find a Map of all possible Dto Classes for auto discovery.
	 *
	 * @param environment  The current environment.
	 * @param dtoNamespace A namespace for possible restriction to the search.
	 * @return A Map of all Classes that might be used.
	 */
	private Map<String, List<TypeName>> findDtoClasses(@NotNull RoundEnvironment environment, @Nullable String dtoNamespace) {
		final Map<String, List<TypeName>> dtoClasses = new HashMap<>();
		environment.getRootElements().forEach(element -> {
			if (dtoNamespace == null || dtoNamespace.isEmpty() || element.toString().startsWith(dtoNamespace)) {
				String simpleName = element.getSimpleName().toString();
				if (!dtoClasses.containsKey(simpleName)) {
					dtoClasses.put(simpleName, new ArrayList<>());
				}
				dtoClasses.get(simpleName).add(TypeName.get(element.asType()));
			}
		});
		return dtoClasses;
	}
}
