package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.injector.AuthenticationInjector;
import eu.nerdfactor.restness.code.injector.ReturnStatementInjector;
import eu.nerdfactor.restness.config.AccessorType;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.config.RelationConfiguration;
import eu.nerdfactor.restness.config.SecurityConfiguration;
import eu.nerdfactor.restness.util.RestnessUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder class responsible for generating the method that retrieves a single
 * related entity associated with a main entity within a REST controller.
 * The method identifies the main entity using its ID from the path variable
 * and returns the related entity (or DTO).
 */
@Slf4j
@With
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class GetSingleRelationMethodBuilder extends MethodBuilder {

	/**
	 * List of existing request mappings in the controller to avoid duplicates.
	 */
	protected List<String> existingRequestMappings;

	/**
	 * The base request path for the controller.
	 */
	private String requestBasePath;

	/**
	 * The class name of the main entity.
	 */
	private TypeName entityClassName;

	/**
	 * The class name of the main entity's ID.
	 */
	private TypeName idClassName;

	/**
	 * The security configuration for the controller.
	 */
	private SecurityConfiguration securityConfiguration;

	/**
	 * The class name for the response wrapper, if any.
	 */
	private TypeName responseWrapperClassName;

	/**
	 * The name of the relation property.
	 */
	private String relationName;

	/**
	 * Indicates if the relation uses a DTO.
	 */
	private boolean relationUsesDto;

	/**
	 * The class name of the relation's response object (DTO or entity).
	 */
	private TypeName relationResponseObjectClassName;

	/**
	 * The class name of the related entity.
	 */
	private TypeName relationEntityClassName; // Changed from ClassName for consistency

	/**
	 * The name of the getter method for the relation in the main entity.
	 */
	private String relationGetterMethodName;

	/**
	 * Static factory method to create a new instance of {@link GetSingleRelationMethodBuilder}.
	 *
	 * @return A new instance of {@link GetSingleRelationMethodBuilder}.
	 */
	public static GetSingleRelationMethodBuilder create() {
		return new GetSingleRelationMethodBuilder();
	}

	/**
	 * Configures the builder with general controller settings.
	 *
	 * @param configuration The controller configuration.
	 * @return The builder instance for chaining.
	 */
	@Override
	public GetSingleRelationMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		return this.withExistingRequestMappings(configuration.getExistingRequestMappings())
				.withRequestBasePath(configuration.getRequestBasePath())
				.withEntityClassName(configuration.getEntityClassName())
				.withIdClassName(configuration.getIdClassName())
				.withSecurityConfiguration(configuration.getSecurityConfiguration())
				.withResponseWrapperClassName(configuration.getResponseWrapperClassName());
	}

	/**
	 * Configures the builder with specific relation settings.
	 *
	 * @param relation The relation configuration.
	 * @return The builder instance for chaining.
	 */
	public GetSingleRelationMethodBuilder withRelation(RelationConfiguration relation) {
		return this.withRelationName(relation.getRelationName())
				.withRelationUsesDto(relation.isUsingDto())
				.withRelationResponseObjectClassName(relation.getResponseObjectClassName())
				.withRelationEntityClassName(relation.getEntityClassName()) // Ensure TypeName is used
				.withRelationGetterMethodName(relation.getGetterMethodName());
	}

	/**
	 * Adds the generated "get single relation" method to the provided
	 * {@link TypeSpec.Builder}, if it doesn't already exist.
	 *
	 * @param builder The {@link TypeSpec.Builder} for the controller class.
	 * @return The modified {@link TypeSpec.Builder}.
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		if (this.hasExistingRequest()) {
			log.debug("Skipping get single relation method for {} as it already exists.", this.relationName);
			return builder;
		}
		addGetSingleRelationMethod(builder);
		return builder;
	}

	/**
	 * Generates and adds the method for retrieving a single related entity.
	 * The method retrieves the main entity, accesses its relation,
	 * maps the related entity to a DTO if configured, and returns it.
	 *
	 * @param builder The {@link TypeSpec.Builder} for the controller class.
	 */
	private void addGetSingleRelationMethod(TypeSpec.Builder builder) {
		log.info("addGetSingleRelationMethod for relation {}", this.relationName);

		String methodName = RestnessUtil.getRelationMethodName(this.relationName, AccessorType.GET);
		String path = this.requestBasePath + "/{id}/" + this.relationName;

		// Determine the actual type of the object to be returned in the response body
		TypeName responseBodyType = this.relationUsesDto && this.relationResponseObjectClassName != null && !this.relationResponseObjectClassName.equals(TypeName.OBJECT)
				? this.relationResponseObjectClassName
				: this.relationEntityClassName;

		// No specific parameters beyond the main entity ID handled by the common builder.
		Consumer<MethodSpec.Builder> parameterConfigurer = mb -> {
		};

		Consumer<MethodSpec.Builder> bodyConfigurer = mb -> {
			mb.addStatement("$T entity = this.dataAccessor.readData(id).orElseThrow($T::new)", this.entityClassName, EntityNotFoundException.class);
			if (this.relationUsesDto) {
				mb.addStatement("$T response = this.dataMapper.map(entity." + this.relationGetterMethodName + "(), $T.class)", responseBodyType, responseBodyType);
			} else {
				mb.addStatement("$T response = entity." + this.relationGetterMethodName + "()", responseBodyType);
			}
			// Return statement is handled by the postBodyConfigurer
		};

		Consumer<MethodSpec.Builder> postBodyConfigurer = mb -> new ReturnStatementInjector()
				.withWrapper(this.responseWrapperClassName)
				.withResponse(responseBodyType) // Injector needs the actual response body type
				.withResponseVariable("response") // The variable holding the response object
				.inject(mb);

		MethodSpec methodSpec = buildRelationMethodSpec(methodName, path, responseBodyType, parameterConfigurer, bodyConfigurer, postBodyConfigurer);
		builder.addMethod(methodSpec);
	}

	/**
	 * Helper method to build the common structure of the {@link MethodSpec} for
	 * the "get single relation" method. This includes annotations, modifiers,
	 * the main entity ID path variable, security injection, and return type definition.
	 *
	 * @param methodName          The name for the generated method.
	 * @param path                The request mapping path for the method.
	 * @param responseBodyType    The {@link TypeName} for the object in the response body (DTO or entity).
	 * @param parameterConfigurer A {@link Consumer} to add specific parameters (usually none for GET single).
	 * @param bodyConfigurer      A {@link Consumer} to add the main method body statements.
	 * @param postBodyConfigurer  A {@link Consumer} to add statements after the main body (for response generation).
	 * @return The constructed {@link MethodSpec}.
	 */
	private MethodSpec buildRelationMethodSpec(String methodName, String path, TypeName responseBodyType, Consumer<MethodSpec.Builder> parameterConfigurer, Consumer<MethodSpec.Builder> bodyConfigurer, Consumer<MethodSpec.Builder> postBodyConfigurer) {
		MethodSpec.Builder methodBuilder = MethodSpec
				.methodBuilder(methodName)
				.addAnnotation(AnnotationSpec.builder(GetMapping.class)
						.addMember("value", "$S", path)
						.build())
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec.builder(this.idClassName, "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				);

		// Add specific parameters (usually none needed here)
		parameterConfigurer.accept(methodBuilder);

		// Inject security checks
		methodBuilder = new AuthenticationInjector()
				.withMethod("READ") // Reading a relation requires READ permission
				.withEntityClassName(this.entityClassName)
				.withRelatedClassName(this.relationEntityClassName) // Use the entity class name for security check
				.withSecurityConfig(this.securityConfiguration)
				.inject(methodBuilder);

		// Determine the final return type (potentially wrapped ResponseEntity)
		TypeName finalReturnType;
		if (this.responseWrapperClassName != null && !this.responseWrapperClassName.equals(TypeName.OBJECT)) {
			// Wrap the response body type if a response wrapper is specified
			finalReturnType = ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), ParameterizedTypeName.get(ClassName.bestGuess(this.responseWrapperClassName.toString()), responseBodyType));
		} else {
			// Return ResponseEntity<ResponseBodyType> directly
			finalReturnType = ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseBodyType);
		}
		methodBuilder.returns(finalReturnType);

		// Add the main method body logic
		bodyConfigurer.accept(methodBuilder);

		// Add post-body logic (response generation)
		if (postBodyConfigurer != null) {
			postBodyConfigurer.accept(methodBuilder);
		}

		return methodBuilder.build();
	}

	/**
	 * Checks if a request mapping for getting a single relation
	 * (GET on /base/{id}/relationName) already exists.
	 *
	 * @return {@code true} if a matching mapping exists, {@code false} otherwise.
	 */
	protected boolean hasExistingRequest() {
		return RestnessUtil.hasExistingRequest(
				this.existingRequestMappings,
				this.requestBasePath + "/{id}/" + this.relationName,
				RequestMethod.GET
		);
	}
}