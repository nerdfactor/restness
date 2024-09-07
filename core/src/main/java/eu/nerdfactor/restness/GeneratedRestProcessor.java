package eu.nerdfactor.restness;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.restness.annotation.GeneratedRestConfiguration;
import eu.nerdfactor.restness.annotation.GeneratedRestController;
import eu.nerdfactor.restness.annotation.GeneratedRestSecurity;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.config.SecurityConfiguration;
import eu.nerdfactor.restness.export.GeneratedRestExporter;
import eu.nerdfactor.restness.export.JavaClassExporter;
import eu.nerdfactor.restness.util.AnnotationValueExtractor;
import eu.nerdfactor.restness.util.GeneratedRestUtil;
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
 * Annotation processor for generated rest controllers.
 * Will check all GeneratedRestController and GeneratedRestConfiguration annotations and
 * build new controller classes out of the information.
 * <p>
 * <a href="https://stackoverflow.com/a/31358366">How to debug</a>
 * In directory of pom: mvnDebug clean test
 *
 * @author Daniel Klug
 */
@SupportedAnnotationTypes({
		"eu.nerdfactor.springutil.generatedrest.annotation.GeneratedRestController",
		"eu.nerdfactor.springutil.generatedrest.annotation.GeneratedRestConfiguration"
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class GeneratedRestProcessor extends AbstractProcessor {

	private Filer filer;
	private Elements elementUtils;
	private Messager messanger;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnvironment) {
		super.init(processingEnvironment);
		this.filer = processingEnvironment.getFiler();
		this.elementUtils = processingEnvironment.getElementUtils();
		this.messanger = processingEnvironment.getMessager();
	}

	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
		final Map<String, ControllerConfiguration> controllers = new HashMap<>();

		// Get all values from DynamicRestConfiguration annotations into one map.
		final Map<String, String> generatedConfig = new HashMap<>();
		for (Element element : roundEnvironment.getElementsAnnotatedWith(GeneratedRestConfiguration.class)) {
			if (element.getKind() != ElementKind.CLASS) {
				return true;
			}
			new AnnotationValueExtractor()
					.withUtils(this.elementUtils)
					.withElement(element)
					.forClass(GeneratedRestConfiguration.class)
					.extractInto(generatedConfig);

			GeneratedRestUtil.LOG = !generatedConfig.getOrDefault("log", "false").equals("false");

			GeneratedRestUtil.log("GeneratedConfig");
			generatedConfig.forEach((name, value) -> {
				GeneratedRestUtil.log(name + ": " + value, 1);
			});
		}

		// Get all DynamicRestController annotations and gather information from the specified
		// entity in order to create a ControllerConfiguration.
		this.findControllerValues(roundEnvironment).forEach(wrapper -> {
			ControllerConfiguration config = ControllerConfiguration.builder()
					.fromElement(wrapper.getElement())
					.withUtils(this.elementUtils)
					.withEnvironment(roundEnvironment)
					.withAnnotatedValues(wrapper.getValues())
					.withPrefix(generatedConfig.getOrDefault("classNamePrefix", "Generated"))
					.withPattern(generatedConfig.getOrDefault("classNamePattern", "{PREFIX}{NAME}"))
					.withDataWrapper(ClassName.bestGuess(generatedConfig.getOrDefault("dataWrapper", Object.class.getCanonicalName())))
					.withDtoClasses(this.findDtoClasses(roundEnvironment, generatedConfig.getOrDefault("dtoNamespace", "")))
					.build();
			controllers.put(config.getClassName().simpleName(), config);
		});

		// Get all DynamicRestSecurity annotations and add them to the matching controllers.
		for (Element element : roundEnvironment.getElementsAnnotatedWith(GeneratedRestSecurity.class)) {
			if (element.getKind() != ElementKind.CLASS) {
				return true;
			}
			SecurityConfiguration security = SecurityConfiguration.builder()
					.fromElement(element)
					.withUtils(this.elementUtils)
					.withEnvironment(roundEnvironment)
					.withPrefix(generatedConfig.getOrDefault("classNamePrefix", "Generated"))
					.withPattern(generatedConfig.getOrDefault("classNamePattern", "{PREFIX}{NAME}"))
					.build();
			if (controllers.containsKey(security.getClassName().simpleName())) {
				controllers.get(security.getClassName().simpleName()).setSecurity(security);
			}
		}

		// Take the ControllerConfigurations and build new classes from them.
		String exporterClassName = generatedConfig.getOrDefault("exporter", JavaClassExporter.class.getCanonicalName());
		try {
			// todo: maybe a factory is better?
			Class cls = Class.forName(exporterClassName);
			GeneratedRestExporter exporter = (GeneratedRestExporter) cls.getDeclaredConstructor().newInstance();
			exporter.withFiler(this.filer)
					.export(generatedConfig, controllers);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	private List<AnnotationValueExtractor.ValueWrapper> findControllerValues(RoundEnvironment roundEnvironment) {
		List<AnnotationValueExtractor.ValueWrapper> controllerValues = new ArrayList<>();
		for (Element element : roundEnvironment.getElementsAnnotatedWith(GeneratedRestController.List.class)) {
			controllerValues.addAll(new AnnotationValueExtractor()
					.forClass(GeneratedRestController.List.class)
					.withElement(element)
					.withUtils(this.elementUtils)
					.extractList());
		}
		for (Element element : roundEnvironment.getElementsAnnotatedWith(GeneratedRestController.class)) {
			controllerValues.add(new AnnotationValueExtractor()
					.forClass(GeneratedRestController.class)
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
