package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.injector.MethodInjectorRegistry;
import eu.nerdfactor.restness.code.injector.MethodContext;
import eu.nerdfactor.restness.code.injector.RelationAuthenticationInjector;
import eu.nerdfactor.restness.code.injector.OpenApiAnnotationInjector;
import eu.nerdfactor.restness.config.AccessorType;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.config.RelationConfiguration;
import eu.nerdfactor.restness.config.SecurityConfiguration;
import eu.nerdfactor.restness.util.RestnessUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder class responsible for generating the method that sets or updates a single
 * related entity associated with a main entity within a REST controller.
 * The method identifies the main entity using its ID from the path variable,
 * takes the related entity (or DTO) from the request body, updates the relation,
 * and typically returns the updated related entity (or DTO) by delegating to the GET method.
 *
 * @author Daniel Klug
 */
@Slf4j
@With
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SetSingleRelationMethodBuilder extends MethodBuilder {

	/**
	 * List of existing request mappings in the controller to avoid duplicates.
	 */
	protected List<String> requestMappings;

	/**
	 * The base path for the controller's request mappings.
	 */
	private String basePath;

	/**
	 * The class name of the main entity's identifier.
	 */
	private TypeName idType;

	/**
	 * The class name of the main entity.
	 */
	private TypeName entityType;

	/**
	 * The security configuration for the controller.
	 */
	private SecurityConfiguration securityConfig;

	/**
	 * The class name of the response wrapper, if configured.
	 */
	private TypeName responseWrapperType;

	/**
	 * The name of the relation.
	 */
	private String relationName;

	/**
	 * Indicates if the relation uses a DTO for request/response.
	 */
	private boolean isUsingDto;

	/**
	 * The class name of the relation's request/response object (DTO or entity).
	 */
	private TypeName relationResponseType;

	/**
	 * The class name of the related entity.
	 */
	private TypeName relationEntityType;

	/**
	 * The name of the setter method for the relation on the main entity.
	 */
	private String relationSetter;

	/**
	 * Flag indicating if OpenAPI annotations should be generated for the method.
	 */
	private boolean openApi;

	/**
	 * The {@link MethodInjectorRegistry} for applying custom injectors to this method.
	 */
	private MethodInjectorRegistry injectorRegistry;

	/**
	 * Static factory method to create a new instance of {@link SetSingleRelationMethodBuilder}.
	 *
	 * @return A new instance of {@link SetSingleRelationMethodBuilder}.
	 */
	public static SetSingleRelationMethodBuilder create() {
		return new SetSingleRelationMethodBuilder();
	}

	/**
	 * Configures the builder with general controller settings.
	 *
	 * @param configuration The controller configuration.
	 * @return The builder instance for chaining.
	 */
	public SetSingleRelationMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		return this.withRequestMappings(configuration.getExistingRequestMappings())
				.withBasePath(configuration.getRequestBasePath())
				.withIdType(configuration.getIdType())
				.withEntityType(configuration.getEntityType())
				.withSecurityConfig(configuration.getSecurityConfig())
				.withResponseWrapperType(configuration.getResponseWrapperType())
				.withOpenApi(configuration.isOpenApi());
	}

	/**
	 * Configures the builder with specific relation settings.
	 *
	 * @param relation The {@link RelationConfiguration} to use.
	 * @return The current {@link SetSingleRelationMethodBuilder} instance.
	 */
	public SetSingleRelationMethodBuilder withRelation(RelationConfiguration relation) {
		// Determine the class name for request/response based on DTO usage
		TypeName dtoOrEntity = relation.isUsingDto() && relation.getResponseObjectType() != null && !relation.getResponseObjectType().equals(TypeName.OBJECT)
				? relation.getResponseObjectType()
				: relation.getEntityType();

		return this.withRelationName(relation.getRelationName())
				.withUsingDto(relation.isUsingDto())
				.withRelationResponseType(dtoOrEntity) // Use the determined class
				.withRelationEntityType(relation.getEntityType())
				.withRelationSetter(relation.getSetterMethodName());
	}

	/**
	 * Adds the generated "set single relation" method to the provided
	 * {@link TypeSpec.Builder}, if it doesn't already exist.
	 *
	 * @param builder The {@link TypeSpec.Builder} for the controller class.
	 * @return The modified {@link TypeSpec.Builder}.
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		if (this.hasExistingRequest()) {
			log.warn("Existing request mapping found for POST/PUT/PATCH on {}/{id}/{}, skipping generation.", this.basePath, this.relationName);
			return builder;
		}
		addSetSingleRelationMethod(builder);
		return builder;
	}

	/**
	 * Generates and adds the method for setting a single related entity.
	 * The method handles POST, PUT, and PATCH requests. It retrieves the main entity,
	 * maps the request body (DTO or entity) to the related entity if necessary,
	 * sets the relation on the main entity, persists the change, and then delegates
	 * to the corresponding GET method to return the result.
	 *
	 * @param builder The {@link TypeSpec.Builder} for the controller class.
	 */
	private void addSetSingleRelationMethod(TypeSpec.Builder builder) {
		log.info("Adding set single relation method for relation: {}", this.relationName);

		String methodName = RestnessUtil.getRelationMethodName(this.relationName, AccessorType.SET);
		String path = this.basePath + "/{id}/" + this.relationName;
		TypeName requestBodyType = this.relationResponseType; // Input type is DTO or Entity
		TypeName responseBodyType = this.relationResponseType; // Output type matches GET method (DTO or Entity)

		Consumer<MethodSpec.Builder> parameterConfigurer = mb -> mb.addParameter(ParameterSpec.builder(requestBodyType, "requestObject") // Changed name
				.addAnnotation(RequestBody.class)
				.addAnnotation(Valid.class)
				.build());

		Consumer<MethodSpec.Builder> bodyConfigurer = mb -> {
			mb.addStatement("$T entity = this.dataAccessor.readData(id).orElseThrow(() -> new $T(\"Entity with id \" + id + \" not found\"))", this.entityType, EntityNotFoundException.class);

			if (this.isUsingDto) {
				mb.addStatement("$T rel = this.dataMapper.map(requestObject, $T.class)", this.relationEntityType, this.relationEntityType);
			} else {
				// If not using DTO, the request body 'requestObject' *is* the relation entity
				mb.addStatement("$T rel = requestObject", this.relationEntityType);
			}

			mb.addStatement("entity." + this.relationSetter + "(rel)");
			mb.addStatement("this.dataAccessor.updateData(entity)");

			// The return statement calls the corresponding GET method to ensure consistency and security checks.
			mb.addStatement("return this." + RestnessUtil.getRelationMethodName(this.relationName, AccessorType.GET) + "(id)");
		};

		// No separate post-body configurer needed as the return statement is part of the main body.
		Consumer<MethodSpec.Builder> postBodyConfigurer = mb -> {
		};

		String entityName = RestnessUtil.toClassName(this.entityType).simpleName();
		String relationCapitalized = this.relationName.substring(0, 1).toUpperCase() + this.relationName.substring(1);

		Consumer<MethodSpec.Builder> openApiConfigurer = mb -> new OpenApiAnnotationInjector()
				.withEnabled(this.openApi)
				.withOperationSummary("Set " + relationCapitalized + " of " + entityName)
				.withOperationId("set" + entityName + relationCapitalized)
				.withResponseCode("200")
				.withResponseDescription(relationCapitalized + " set successfully")
				.addErrorResponse("404", entityName + " not found")
				.inject(mb);

		MethodSpec methodSpec = buildSetRelationMethodSpec(methodName, path, requestBodyType, responseBodyType, parameterConfigurer, openApiConfigurer, bodyConfigurer, postBodyConfigurer);
		builder.addMethod(methodSpec);
	}

	/**
	 * Helper method to build the common structure of the {@link MethodSpec} for
	 * the "set single relation" method. This includes annotations, modifiers,
	 * path variables, request body parameter, OpenAPI annotations, security injection,
	 * and return type definition.
	 *
	 * @param methodName          The name for the generated method.
	 * @param path                The request mapping path for the method.
	 * @param requestBodyType     The {@link TypeName} for the object in the request body (DTO or entity).
	 * @param responseBodyType    The {@link TypeName} for the object in the response body (DTO or entity, matching GET).
	 * @param parameterConfigurer A {@link Consumer} to add specific parameters (like @RequestBody).
	 * @param openApiConfigurer   A {@link Consumer} to add OpenAPI annotations to the method.
	 * @param bodyConfigurer      A {@link Consumer} to add the main method body statements.
	 * @param postBodyConfigurer  A {@link Consumer} to add statements after the main body (usually none for SET).
	 * @return The constructed {@link MethodSpec}.
	 */
	private MethodSpec buildSetRelationMethodSpec(String methodName, String path, TypeName requestBodyType, TypeName responseBodyType, Consumer<MethodSpec.Builder> parameterConfigurer, Consumer<MethodSpec.Builder> openApiConfigurer, Consumer<MethodSpec.Builder> bodyConfigurer, Consumer<MethodSpec.Builder> postBodyConfigurer) {
		MethodSpec.Builder methodBuilder = MethodSpec
				.methodBuilder(methodName)
				.addAnnotation(AnnotationSpec.builder(RequestMapping.class)
						.addMember("value", "$S", path)
						.addMember("method", "{ $T.POST, $T.PUT, $T.PATCH }", RequestMethod.class, RequestMethod.class, RequestMethod.class)
						.build())
				.addAnnotation(ResponseBody.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec.builder(this.idType, "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				);

		// Add @RequestBody parameter
		parameterConfigurer.accept(methodBuilder);

		// Inject OpenAPI annotations
		openApiConfigurer.accept(methodBuilder);

		// Inject security checks
		methodBuilder = new RelationAuthenticationInjector()
				.withMethod("UPDATE") // Setting a relation requires UPDATE permission
				.withEntityClassName(this.entityType)
				.withRelatedClassName(this.relationEntityType) // Check based on the actual related entity
				.withSecurityConfig(this.securityConfig)
				.inject(methodBuilder);

		// Apply custom injectors from SPI registry.
		if (this.injectorRegistry != null) {
			methodBuilder = this.injectorRegistry.injectAll(methodBuilder, MethodContext.builder()
					.withMethodName(methodName)
					.withHttpMethod("POST")
					.withEntityType(this.entityType)
					.withRelatedEntityType(this.relationEntityType)
					.withRelationName(this.relationName)
					.build());
		}

		// Determine the final return type (matching the GET method's return type)
		TypeName finalReturnType;
		if (this.responseWrapperType != null && !this.responseWrapperType.equals(TypeName.OBJECT)) {
			// Wrap the response body type if a response wrapper is specified
			finalReturnType = ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), ParameterizedTypeName.get(ClassName.bestGuess(this.responseWrapperType.toString()), responseBodyType));
		} else {
			// Return ResponseEntity<ResponseBodyType> directly
			finalReturnType = ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseBodyType);
		}
		methodBuilder.returns(finalReturnType);

		// Add the main method body logic
		bodyConfigurer.accept(methodBuilder);

		// Add post-body logic (usually none needed here)
		if (postBodyConfigurer != null) {
			postBodyConfigurer.accept(methodBuilder);
		}

		return methodBuilder.build();
	}

	/**
	 * Checks if a request mapping for setting a single relation
	 * (POST, PUT, or PATCH on /base/{id}/relationName) already exists.
	 *
	 * @return {@code true} if a matching mapping exists, {@code false} otherwise.
	 */
	protected boolean hasExistingRequest() {
		String path = this.basePath + "/{id}/" + this.relationName;
		return RestnessUtil.hasExistingRequest(this.requestMappings, path, RequestMethod.POST) ||
				RestnessUtil.hasExistingRequest(this.requestMappings, path, RequestMethod.PUT) ||
				RestnessUtil.hasExistingRequest(this.requestMappings, path, RequestMethod.PATCH);
	}
}