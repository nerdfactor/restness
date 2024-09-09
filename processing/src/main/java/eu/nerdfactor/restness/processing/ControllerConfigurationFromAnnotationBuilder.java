package eu.nerdfactor.restness.processing;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.restness.annotation.IdAccessor;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.config.RelationConfiguration;
import eu.nerdfactor.restness.data.DataAccessor;
import eu.nerdfactor.restness.data.DataMapper;
import eu.nerdfactor.restness.data.DataMerger;
import eu.nerdfactor.restness.util.RestnessUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.*;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.*;

import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * Builder that creates a new controller configuration from annotations.
 *
 * @author Daniel Klug
 */
public class ControllerConfigurationFromAnnotationBuilder {

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
	 * The {@link TypeName} of a class used as wrapper around the data returned
	 * by the controller.
	 */
	protected TypeName responseWrapperClassName;

	/**
	 * A map of possible Dto Classes.
	 */
	@Deprecated(since = "0.0.20")
	protected Map<String, List<TypeName>> dtoClasses;

	/**
	 * The values from the {@link RestController} configuration.
	 */
	protected Map<String, String> annotatedValues;

	/**
	 * @param env The annotation processing environment round.
	 * @return The builder in a fluent api pattern.
	 */
	public ControllerConfigurationFromAnnotationBuilder withEnvironment(@NotNull RoundEnvironment env) {
		this.environment = env;
		return this;
	}

	/**
	 * @param utils The element utilities during annotation processing.
	 * @return The builder in a fluent api pattern.
	 */
	public ControllerConfigurationFromAnnotationBuilder withUtils(@NotNull Elements utils) {
		this.elementUtils = utils;
		return this;
	}

	/**
	 * @param element The annotated Element.
	 * @return The builder in a fluent api pattern.
	 */
	public ControllerConfigurationFromAnnotationBuilder withElement(@NotNull Element element) {
		this.element = (TypeElement) element;
		return this;
	}

	/**
	 * @param prefix The prefix used during generating the class name.
	 * @return The builder in a fluent api pattern.
	 */
	public ControllerConfigurationFromAnnotationBuilder withPrefix(@NotNull String prefix) {
		this.classNamePrefix = prefix;
		return this;
	}

	/**
	 * @param pattern The pattern used to generate the class name.
	 * @return The builder in a fluent api pattern.
	 */
	public ControllerConfigurationFromAnnotationBuilder withPattern(@NotNull String pattern) {
		this.classNamePattern = pattern;
		return this;
	}

	/**
	 * @param responseWrapper The response wrapper class.
	 * @return The builder in a fluent api pattern.
	 */
	public ControllerConfigurationFromAnnotationBuilder withResponseWrapper(@NotNull TypeName responseWrapper) {
		this.responseWrapperClassName = responseWrapper;
		return this;
	}

	/**
	 * @param dtoClasses A map of possible Dto Classes.
	 * @return The builder in a fluent api pattern.
	 */
	@Deprecated(since = "0.0.20")
	public ControllerConfigurationFromAnnotationBuilder withDtoClasses(@NotNull Map<String, List<TypeName>> dtoClasses) {
		this.dtoClasses = dtoClasses;
		return this;
	}

	/**
	 * @param values The values from the {@link RestController} configuration.
	 * @return The builder in a fluent api pattern.
	 */
	public ControllerConfigurationFromAnnotationBuilder withAnnotatedValues(@NotNull Map<String, String> values) {
		this.annotatedValues = values;
		return this;
	}

	public static ControllerConfigurationFromAnnotationBuilder create() {
		return new ControllerConfigurationFromAnnotationBuilder();
	}

	/**
	 * Collect information about the controller from the annotated class.
	 *
	 * @return {@link ControllerConfiguration} with the found information.
	 */
	public ControllerConfiguration build() {
		// Create parts of the annotated class name.
		String packageName = this.element != null ? elementUtils.getPackageOf(element).getQualifiedName().toString() : "";
		String className = this.element != null ? element.getSimpleName().toString() : "";

		// Create class names from the information.
		ClassName entityClass = ClassName.bestGuess(this.annotatedValues.get("entity"));
		ClassName[] dtoClasses = this.findDtoClasses(entityClass);
		boolean withDto = !entityClass.equals(dtoClasses[0]);
		ClassName idClass = ClassName.bestGuess(this.annotatedValues.get("id"));

		// Combine the generated class name and package.
		String generatedClassName = this.annotatedValues.getOrDefault("className", "");
		if (generatedClassName.length() <= 0) {
			generatedClassName = this.classNamePattern.replace("{PREFIX}", this.classNamePrefix).replace("{NAME}", className).replace("{NAME_NORMALIZED}", className.replace("Controller", ""));
		}
		if (!generatedClassName.contains(".")) {
			generatedClassName = packageName + "." + generatedClassName;
		}

		// Find elements for the specified entity.
		TypeElement entityElement = this.element;
		for (Element elem : this.environment.getRootElements()) {
			if (elem.getSimpleName().toString().equals(entityClass.simpleName())) {
				entityElement = (TypeElement) elem;
			}
		}

		ParameterizedTypeName dataAccessorClass = ParameterizedTypeName.get(ClassName.get(DataAccessor.class), entityClass, idClass);
		ClassName dataMergerClass = ClassName.get(DataMerger.class);
		ClassName dataMapperClass = ClassName.get(DataMapper.class);

		// Check how the id can be accessed in the entity.
		String idAccessor = "getId";
		for (ExecutableElement method : methodsIn(entityElement.getEnclosedElements())) {
			for (AnnotationMirror anno : method.getAnnotationMirrors()) {
				String annotationName = anno.getAnnotationType().toString();
				if (annotationName.equals(IdAccessor.class.getName())) {
					idAccessor = method.getSimpleName().toString();
				}
			}
		}

		// Check for existing requests in the annotated class.
		List<String> existingRequests = new ArrayList<>();
		if (this.element != null) {
			for (ExecutableElement method : methodsIn(this.element.getEnclosedElements())) {
				for (AnnotationMirror anno : method.getAnnotationMirrors()) {
					Arrays.asList(RequestMapping.class, GetMapping.class, PostMapping.class, PutMapping.class, PatchMapping.class, DeleteMapping.class).forEach(cls -> {
						if (cls.getCanonicalName().equals(anno.getAnnotationType().toString())) {
							Map<String, String> requestMappingAnnotatedValues = new AnnotationValueExtractor()
									.withUtils(this.elementUtils)
									.withElement(method)
									.forClass(cls)
									.extract()
									.values();

							String requestMapping = requestMappingAnnotatedValues.getOrDefault("value", "/").replaceAll("\"$", "").replaceAll("^\"", "");
							if (requestMapping.length() > 1) {
								String clsName = cls.getSimpleName();
								String methodName = clsName.substring(0, clsName.indexOf('M')).toUpperCase();
								List<String> methodNames = new ArrayList<>(Collections.singletonList(methodName));
								if (cls == RequestMapping.class) {
									String[] requestMethods = requestMappingAnnotatedValues.getOrDefault("method", "GET").replaceAll("\"$", "").replaceAll("^\"", "").split(",");
									Arrays.stream(requestMethods).forEach(s -> methodNames.add(s.substring(s.lastIndexOf(".") + 1)));
								}
								methodNames.forEach(m -> {
									if (!m.equals("REQUEST")) {
										existingRequests.add(m + requestMapping.toLowerCase());
									}
								});
							}
						}
					});
				}
			}
		}

		// Get the path for the request mapping from the annotation.
		String requestMapping = this.annotatedValues.getOrDefault("value", "");

		// If the controller should contain relations, collect them from the entity.
		Map<String, RelationConfiguration> relations = new HashMap<>();
		if (this.annotatedValues.get("withRelations").equals("true")) {
			// Get all compiled classes in order to determine dto for entity.


			// Collect all the relations.
			relations = RelationConfigurationFromAnnotationBuilder.create().withElement(entityElement).withUtils(this.elementUtils).withClasses(this.dtoClasses).withDtos(withDto).build();
		}

		return new ControllerConfiguration(RestnessUtil.toClassName(generatedClassName), requestMapping, entityClass, idClass, idAccessor, withDto ? dtoClasses[0] : null, withDto ? dtoClasses[1] : null, withDto ? dtoClasses[2] : null, this.responseWrapperClassName, dataAccessorClass, dataMergerClass, dataMapperClass, existingRequests, null, relations);
	}

	protected ClassName[] findDtoClasses(ClassName entityClass) {
		ClassName dtoClass = this.findConfiguredDtoClassInAnnotatedValues("dtoConfig/value", "dto", null);
		if (dtoClass.equals(ClassName.OBJECT)) {
			dtoClass = entityClass;
		}
		ClassName dtoListClass = this.findConfiguredDtoClassInAnnotatedValues("dtoConfig/list", "dtoConfig/value", "dto");
		if (dtoListClass.equals(ClassName.OBJECT)) {
			dtoListClass = dtoClass;
		}
		ClassName dtoRequestClass = this.findConfiguredDtoClassInAnnotatedValues("dtoConfig/request", "dtoConfig/value", "dto");
		if (dtoRequestClass.equals(ClassName.OBJECT)) {
			dtoRequestClass = dtoClass;
		}
		return List.of(dtoClass, dtoListClass, dtoRequestClass).toArray(new ClassName[]{});
	}

	protected @NotNull ClassName findConfiguredDtoClassInAnnotatedValues(@NotNull String primaryChoice, @Nullable String secondaryChoice, @Nullable String tertiaryChoice) {
		String className = Object.class.getCanonicalName();
		if (!this.annotatedValues.getOrDefault(primaryChoice, className).equals(className)) {
			className = this.annotatedValues.get(primaryChoice);
		}
		if (secondaryChoice != null && className.equals(Object.class.getCanonicalName()) && !this.annotatedValues.getOrDefault(secondaryChoice, className).equals(className)) {
			className = this.annotatedValues.get(secondaryChoice);
		}
		if (tertiaryChoice != null && className.equals(Object.class.getCanonicalName()) && !this.annotatedValues.getOrDefault(tertiaryChoice, className).equals(className)) {
			className = this.annotatedValues.get(tertiaryChoice);
		}
		return ClassName.bestGuess(className);
	}
}