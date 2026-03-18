package eu.nerdfactor.restness.processing;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.restness.annotation.IdAccessor;
import eu.nerdfactor.restness.annotation.IdModifier;
import jakarta.persistence.Id;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.config.RelationConfiguration;
import eu.nerdfactor.restness.config.SecurityConfiguration;
import eu.nerdfactor.restness.data.DataAccessor;
import eu.nerdfactor.restness.data.DataMapper;
import eu.nerdfactor.restness.data.DataMerger;
import eu.nerdfactor.restness.processing.extractor.AnnotationValueExtractor;
import eu.nerdfactor.restness.processing.extractor.ValueContainer;
import eu.nerdfactor.restness.util.RestnessUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.bind.annotation.*;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.*;

import static javax.lang.model.util.ElementFilter.fieldsIn;
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

	public static ControllerConfigurationFromAnnotationBuilder create() {
		return new ControllerConfigurationFromAnnotationBuilder();
	}

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

	/**
	 * Collect information about the controller from the annotated class.
	 *
	 * @return {@link ControllerConfiguration} with the found information.
	 */
	public ControllerConfiguration build() {
		// Create parts of the annotated class name.
		String packageName = this.element != null ? elementUtils.getPackageOf(element).getQualifiedName().toString() : "";
		String className = this.element != null ? element.getSimpleName().toString() : "";

		// Find the entity class, the dto classes and class of the id from annotation values.
		ClassName entityClass = ClassName.bestGuess(this.annotatedValues.get("entity"));
		ClassName[] dtoClasses = this.findDtoClasses(entityClass);
		boolean withDto = !entityClass.equals(dtoClasses[0]);
		ClassName idClass = ClassName.bestGuess(this.annotatedValues.get("id"));

		// Combine the generated class name and package.
		ClassName generatedClassName = findGeneratedClassName(className, packageName);

		// Find elements for the specified entity.
		TypeElement entityElement = this.element;
		for (Element elem : this.environment.getRootElements()) {
			if (elem.getSimpleName().toString().equals(entityClass.simpleName())) {
				entityElement = (TypeElement) elem;
			}
		}

		// Check how the id can be accessed in the entity.
		String idAccessor = this.findIdActor(entityElement, IdAccessor.class);
		String idModifier = this.findIdActor(entityElement, IdModifier.class);

		// Check for existing requests in the annotated class.
		List<String> existingRequests = checkForExistingRequestMethods();

		// Get the path for the request mapping from the annotation.
		String requestMapping = this.annotatedValues.getOrDefault("value", "");

		// If the controller should contain relations, collect them from the entity.
		Map<String, RelationConfiguration> relations = new HashMap<>();
		if (this.annotatedValues.get("withRelations").equals("true")) {
			// Get all compiled classes in order to determine dto for entity. <- why is this comment here but no matching code?
			// Collect all the relations.
			relations = RelationConfigurationFromAnnotationBuilder.create().withElement(entityElement).withUtils(this.elementUtils).withClasses(this.dtoClasses).withDtos(withDto).build();
		}

		// setup data manipulation classes.
		ParameterizedTypeName dataAccessorClass = ParameterizedTypeName.get(ClassName.get(DataAccessor.class), entityClass, idClass);
		ClassName dataMergerClass = ClassName.get(DataMerger.class);
		ClassName dataMapperClass = ClassName.get(DataMapper.class);

		// the security configuration will be injected later by the annotation processor
		// this definition is just for clarity and understandability of the code.
		SecurityConfiguration securityConfig = null;

		return new ControllerConfiguration(generatedClassName, requestMapping, entityClass, idClass, idAccessor, idModifier, withDto ? dtoClasses[0] : null, withDto ? dtoClasses[1] : null, withDto ? dtoClasses[2] : null, this.responseWrapperClassName, dataAccessorClass, dataMergerClass, dataMapperClass, existingRequests, securityConfig, false, relations);
	}

	/**
	 * Finds the correct name for the generated class from the annotations and the
	 * name and package of the annotated class.
	 *
	 * @param className   The name of the annotated class.
	 * @param packageName The package of the annotated class.
	 * @return The name for the generated class.
	 */
	private @NotNull ClassName findGeneratedClassName(String className, String packageName) {
		return RestnessUtil.resolveGeneratedClassName(
				this.annotatedValues.getOrDefault("className", ""),
				this.classNamePattern,
				this.classNamePrefix,
				className,
				packageName
		);
	}

	/**
	 * Check for existing RequestMappings in the annotated class to avoid implementing
	 * the same request twice.
	 *
	 * @return A list of existing request mappings.
	 */
	private @NotNull List<String> checkForExistingRequestMethods() {
		List<String> existingRequests = new ArrayList<>();
		if (this.element != null) {
			// get all methods in the annotated class
			for (ExecutableElement method : methodsIn(this.element.getEnclosedElements())) {
				// and check all annotations of the method
				for (AnnotationMirror anno : method.getAnnotationMirrors()) {
					// for existing request mappings annotations.
					Arrays.asList(RequestMapping.class, GetMapping.class, PostMapping.class, PutMapping.class, PatchMapping.class, DeleteMapping.class).forEach(cls -> checkForExistingRequestMappingType(method, anno, cls, existingRequests));
				}
			}
		}
		return existingRequests;
	}

	/**
	 * Check for an existing Mapping Annotation of a given type.
	 *
	 * @param method           The element of the method to check for an existing mapping annotation.
	 * @param anno             The annotation mirror of the method.
	 * @param cls              The class of the mapping annotation to check for.
	 * @param existingRequests The list of existing requests to add the new request to.
	 */
	private void checkForExistingRequestMappingType(ExecutableElement method, AnnotationMirror anno, Class<? extends Annotation> cls, List<String> existingRequests) {
		if (cls.getCanonicalName().equals(anno.getAnnotationType().toString())) {
			ValueContainer annotatedValues = new AnnotationValueExtractor()
					.withUtils(this.elementUtils)
					.withElement(method)
					.forClass(cls)
					.extract();

			// get the url path from the annotation
			String requestMapping = getRequestMappingPath(annotatedValues);
			// todo: why are we just checking for urls that are not empty?
			if (requestMapping.length() > 1) {
				// cut the RequestMethod from the RequestMapping class name
				// todo: this is a bit hacky, but it works for now. change this to a better solution.
				String clsName = cls.getSimpleName();
				String methodName = clsName.substring(0, clsName.indexOf('M')).toUpperCase();
				List<String> methodNames = new ArrayList<>(Collections.singletonList(methodName));
				if (cls == RequestMapping.class) {
					// if the class is RequestMapping, we need to get the method from the annotation
					// because it can handle multiple request methods (GET, POST, PUT, DELETE) like:
					// @RequestMapping(value="/api/v1/entity/{id}", method={GET, POST, PUT, DELETE})
					String[] requestMethods = annotatedValues.getStringOrDefault("method", "GET").replaceAll("\"$", "").replaceAll("^\"", "").split(",");
					Arrays.stream(requestMethods).forEach(s -> methodNames.add(s.substring(s.lastIndexOf(".") + 1)));
				}
				// skip the REQUEST method, because it is not a real request method and just a artifact of the RequestMapping class
				methodNames.stream().filter(x -> !x.equals("REQUEST")).forEach(m -> {
					// add the request mapping to the list of existing RequestMethods
					existingRequests.add(m + requestMapping.toLowerCase());
				});
			}
		}
	}

	/**
	 * Get the request mapping path from the annotated values.
	 * This will be a url path like /api/v1/entity/{id}.
	 * Will normalize the path by removing leading and trailing quotes that might be
	 * added by the annotation processor.
	 *
	 * @param annotatedValues The annotated values.
	 * @return The request mapping path.
	 */
	private static @NotNull String getRequestMappingPath(ValueContainer annotatedValues) {
		return annotatedValues.getStringOrDefault("value", "/").replaceAll("\"$", "").replaceAll("^\"", "");
	}

	/**
	 * Find the id actor in the entity class.
	 *
	 * @param entityElement   The entity element.
	 * @param annotationClass The annotation class to search for.
	 * @return The name of the id actor.
	 */
	private String findIdActor(TypeElement entityElement, Class<?> annotationClass) {
		// First, check for explicit @IdAccessor or @IdModifier annotations on methods.
		for (ExecutableElement method : methodsIn(entityElement.getEnclosedElements())) {
			for (AnnotationMirror anno : method.getAnnotationMirrors()) {
				if (anno.getAnnotationType().toString().equals(annotationClass.getName())) {
					return method.getSimpleName().toString();
				}
			}
		}
		// Second, check for @jakarta.persistence.Id on fields to derive the accessor name.
		String verb = annotationClass == IdAccessor.class ? "get" : "set";
		for (VariableElement field : fieldsIn(entityElement.getEnclosedElements())) {
			for (AnnotationMirror anno : field.getAnnotationMirrors()) {
				if (anno.getAnnotationType().toString().equals(Id.class.getName())) {
					String fieldName = field.getSimpleName().toString();
					return verb + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
				}
			}
		}
		// Fallback to default getId/setId.
		return verb + "Id";
	}

	/**
	 * @deprecated todo: is this actually used?
	 */
	@Deprecated
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

	/**
	 * Find the configured Dto class in the annotated values by checking multiple possible
	 * configuration options.
	 *
	 * @deprecated todo: is this actually used?
	 */
	@Deprecated
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