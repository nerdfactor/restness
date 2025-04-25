package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.injector.AuthenticationInjector;
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
 * Builder class responsible for generating methods that add entities
 * to a collection relation of a main entity within a REST controller.
 * It generates two types of methods: one that accepts the related entity
 * object (or DTO) in the request body, and another that accepts the ID
 * of the related entity as a path variable.
 *
 * @author Daniel Klug
 */
@Slf4j
@With
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AddToRelationsMethodBuilder extends MethodBuilder {

	/**
	 * List of existing request mappings in the controller to avoid duplicates.
	 */
	protected List<String> requestMappings;
	/**
	 * Base path for the generated controller methods.
	 */
	protected String basePath;
	/**
	 * Class name of the main entity's identifier.
	 */
	protected TypeName idType;
	/**
	 * Class name of the main entity.
	 */
	protected TypeName entityType;
	/**
	 * Security configuration for the generated methods.
	 */
	protected SecurityConfiguration securityConfig;
	/**
	 * Class name for the response wrapper, if any.
	 */
	protected TypeName responseWrapperType;
	/**
	 * Name of the relation.
	 */
	protected String relationName;
	/**
	 * Flag indicating if Data Transfer Objects (DTOs) are used for the relation.
	 */
	protected boolean isUsingDto;
	/**
	 * Class name of the response object for the relation (might be a DTO or the entity itself).
	 */
	protected TypeName relationResponseType;
	/**
	 * Class name of the related entity.
	 */
	protected TypeName relationEntityType;
	/**
	 * Class name of the related entity's identifier.
	 */
	protected TypeName relationIdType;
	/**
	 * Name of the method to access the ID of the related entity/DTO.
	 */
	protected String relationIdAccessor;
	/**
	 * Name of the method on the main entity to add a related entity to the collection.
	 */
	protected String relationAdder;

	/**
	 * Static factory method to create a new instance of {@link AddToRelationsMethodBuilder}.
	 *
	 * @return A new instance of {@link AddToRelationsMethodBuilder}.
	 */
	public static AddToRelationsMethodBuilder create() {
		return new AddToRelationsMethodBuilder();
	}

	/**
	 * Configures the builder with general controller settings.
	 *
	 * @param configuration The controller configuration.
	 * @return The builder instance for chaining.
	 */
	public AddToRelationsMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		return this.withRequestMappings(configuration.getExistingRequestMappings())
				.withBasePath(configuration.getRequestBasePath())
				.withIdType(configuration.getIdType())
				.withEntityType(configuration.getEntityType())
				.withSecurityConfig(configuration.getSecurityConfig())
				.withResponseWrapperType(configuration.getResponseWrapperType());
	}

	/**
	 * Configures the builder with specific relation settings.
	 *
	 * @param relation The relation configuration.
	 * @return The builder instance for chaining.
	 */
	public AddToRelationsMethodBuilder withRelation(RelationConfiguration relation) {
		return this.withRelationName(relation.getRelationName())
				.withUsingDto(relation.isUsingDto())
				.withRelationResponseType(relation.getResponseObjectType())
				.withRelationEntityType(relation.getEntityType())
				.withRelationIdType(relation.getIdType())
				.withRelationIdAccessor(relation.getIdAccessorMethodName())
				.withRelationAdder(relation.getAdderMethodName());
	}

	/**
	 * Adds the generated "add to relation" methods (by object and by ID)
	 * to the provided {@link TypeSpec.Builder}, if they don't already exist.
	 *
	 * @param builder The {@link TypeSpec.Builder} for the controller class.
	 * @return The modified {@link TypeSpec.Builder}.
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		TypeName responseEntityType = this.isUsingDto && this.relationResponseType != null && !this.relationResponseType.equals(TypeName.OBJECT) ? this.relationResponseType : this.relationEntityType;
		ParameterizedTypeName responseListType = ParameterizedTypeName.get(ClassName.get(List.class), responseEntityType);

		if (!this.hasExistingRequest()) {
			addAddToRelationsMethod(builder, responseEntityType, responseListType);
		}

		if (!this.hasExistingRequestById()) {
			addAddToRelationsByIdMethod(builder, responseListType, responseEntityType);
		}

		return builder;
	}

	/**
	 * Generates and adds the method for adding a related entity by its ID.
	 * The method retrieves the main entity, retrieves a reference to the related entity by ID,
	 * adds the related entity to the main entity's collection, updates the main entity,
	 * and returns the updated list of related entities.
	 *
	 * @param builder            The {@link TypeSpec.Builder} for the controller class.
	 * @param responseListType   The {@link ParameterizedTypeName} for the list of response entities (List<ResponseEntityType>).
	 * @param responseEntityType The {@link TypeName} of the individual response entity (DTO or entity).
	 */
	private void addAddToRelationsByIdMethod(TypeSpec.Builder builder, ParameterizedTypeName responseListType, TypeName responseEntityType) {
		log.info("addAddToRelationsByIdMethod");
		String methodName = RestnessUtil.getRelationMethodName(this.relationName, AccessorType.ADD) + "ById";
		String path = this.basePath + "/{id}/" + this.relationName + "/{relationId}";

		Consumer<MethodSpec.Builder> parameterConfigurer = mb -> mb.addParameter(
				ParameterSpec.builder(this.relationIdType, "relationId")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
		);

		Consumer<MethodSpec.Builder> bodyConfigurer = mb -> {
			mb.addStatement("$T entity = this.dataAccessor.readData(id).orElseThrow($T::new)", this.entityType, EntityNotFoundException.class);
			// Use EntityManager.getReference to avoid fetching the full related entity
			mb.addStatement("$T rel = this.entityManager.getReference($T.class, relationId)", this.relationEntityType, this.relationEntityType);
			mb.addStatement("entity." + this.relationAdder + "(rel)");
			mb.addStatement("this.dataAccessor.updateData(entity)");
			// Delegate to the GET method to return the updated list
			mb.addStatement("return this." + RestnessUtil.getRelationMethodName(this.relationName, AccessorType.GET) + "(id)");
		};

		MethodSpec methodSpec = buildRelationMethodSpec(methodName, path, responseListType, responseEntityType, parameterConfigurer, bodyConfigurer);
		builder.addMethod(methodSpec);
	}

	/**
	 * Generates and adds the method for adding a related entity using its DTO/entity representation
	 * provided in the request body. This method typically delegates to the "ById" version
	 * after extracting the ID from the request body object.
	 *
	 * @param builder            The {@link TypeSpec.Builder} for the controller class.
	 * @param responseEntityType The {@link TypeName} of the individual response entity (DTO or entity).
	 * @param responseListType   The {@link ParameterizedTypeName} for the list of response entities (List<ResponseEntityType>).
	 */
	private void addAddToRelationsMethod(TypeSpec.Builder builder, TypeName responseEntityType, ParameterizedTypeName responseListType) {
		log.info("addAddToRelationsMethod");
		String methodName = RestnessUtil.getRelationMethodName(this.relationName, AccessorType.ADD);
		String path = this.basePath + "/{id}/" + this.relationName;

		Consumer<MethodSpec.Builder> parameterConfigurer = mb -> mb.addParameter(
				ParameterSpec.builder(responseEntityType, "dto") // Parameter name changed to 'dto' for clarity
						.addAnnotation(RequestBody.class)
						.addAnnotation(Valid.class) // Add validation
						.build()
		);

		// Delegate to the ById method
		Consumer<MethodSpec.Builder> bodyConfigurer = mb -> mb.addStatement(
				"return this." + RestnessUtil.getRelationMethodName(this.relationName, AccessorType.ADD) + "ById(id, dto." + this.relationIdAccessor + "())"
		);

		MethodSpec methodSpec = buildRelationMethodSpec(methodName, path, responseListType, responseEntityType, parameterConfigurer, bodyConfigurer);
		builder.addMethod(methodSpec);
	}

	/**
	 * Helper method to build the common structure of the {@link MethodSpec} for
	 * the "add to relation" methods. This includes annotations, modifiers,
	 * the main entity ID path variable, security injection, and return type definition.
	 *
	 * @param methodName          The name for the generated method.
	 * @param path                The request mapping path for the method.
	 * @param responseListType    The {@link ParameterizedTypeName} for the list of response entities (List<ResponseEntityType>).
	 * @param responseEntityType  The {@link TypeName} of the individual response entity (DTO or entity).
	 * @param parameterConfigurer A {@link Consumer} to add specific parameters (like relationId or request body).
	 * @param bodyConfigurer      A {@link Consumer} to add the method body statements.
	 * @return The constructed {@link MethodSpec}.
	 */
	private MethodSpec buildRelationMethodSpec(String methodName, String path, ParameterizedTypeName responseListType, TypeName responseEntityType, Consumer<MethodSpec.Builder> parameterConfigurer, Consumer<MethodSpec.Builder> bodyConfigurer) {
		MethodSpec.Builder methodBuilder = MethodSpec
				.methodBuilder(methodName)
				.addAnnotation(AnnotationSpec.builder(RequestMapping.class)
						.addMember("value", "$S", path)
						// Allow POST, PUT, PATCH for adding relations
						.addMember("method", "{ $T.POST, $T.PUT, $T.PATCH }", RequestMethod.class, RequestMethod.class, RequestMethod.class)
						.build())
				.addAnnotation(ResponseBody.class)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec.builder(this.idType, "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				);

		// Add specific parameters (e.g., relationId or request body)
		parameterConfigurer.accept(methodBuilder);

		// Inject security checks
		methodBuilder = new AuthenticationInjector()
				.withMethod("UPDATE") // Adding to a relation is considered an UPDATE operation
				.withEntityClassName(this.entityType)
				.withRelatedClassName(this.relationEntityType)
				.withSecurityConfig(this.securityConfig)
				.inject(methodBuilder);

		// Determine the final return type (potentially wrapped)
		TypeName finalReturnType;
		if (this.responseWrapperType != null && !this.responseWrapperType.equals(TypeName.OBJECT)) {
			// Wrap the list type if a response wrapper is specified
			finalReturnType = ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), ParameterizedTypeName.get(ClassName.bestGuess(this.responseWrapperType.toString()), responseListType));
		} else {
			// Return ResponseEntity<List<ResponseEntityType>> directly
			finalReturnType = ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseListType);
		}
		methodBuilder.returns(finalReturnType);

		// Add the method body logic
		bodyConfigurer.accept(methodBuilder);

		return methodBuilder.build();
	}

	/**
	 * Checks if a request mapping for adding a relation via request body
	 * (POST, PUT, or PATCH on /base/{id}/relationName) already exists.
	 *
	 * @return {@code true} if a matching mapping exists, {@code false} otherwise.
	 */
	protected boolean hasExistingRequest() {
		return RestnessUtil.hasExistingRequest(
				this.requestMappings,
				this.basePath + "/{id}/" + this.relationName,
				RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH
		);
	}

	/**
	 * Checks if a request mapping for adding a relation via relation ID
	 * (POST, PUT, or PATCH on /base/{id}/relationName/{relationId}) already exists.
	 *
	 * @return {@code true} if a matching mapping exists, {@code false} otherwise.
	 */
	protected boolean hasExistingRequestById() {
		return RestnessUtil.hasExistingRequest(
				this.requestMappings,
				this.basePath + "/{id}/" + this.relationName + "/{relationId}",
				RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH
		);
	}
}