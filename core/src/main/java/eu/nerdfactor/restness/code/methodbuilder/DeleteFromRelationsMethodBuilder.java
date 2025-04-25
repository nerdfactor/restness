package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.injector.AuthenticationInjector;
import eu.nerdfactor.restness.code.injector.NoContentStatementInjector;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder class responsible for generating methods that remove entities
 * from a collection relation of a main entity within a REST controller.
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
public class DeleteFromRelationsMethodBuilder extends MethodBuilder {

	/**
	 * List of existing request mappings in the controller to avoid duplicates.
	 */
	protected List<String> requestMappings;

	/**
	 * The base request path from the main controller configuration.
	 */
	protected String basePath;

	/**
	 * The class name of the main entity's ID.
	 */
	protected TypeName idType;

	/**
	 * The class name of the main entity.
	 */
	protected TypeName entityType;

	/**
	 * The security configuration from the main controller configuration.
	 */
	protected SecurityConfiguration securityConfig;

	/**
	 * The class name for the response wrapper, if configured.
	 */
	protected TypeName responseWrapperType;

	/**
	 * The name of the relation.
	 */
	protected String relationName;

	/**
	 * Indicates if the relation uses DTOs.
	 */
	protected boolean isUsingDto;

	/**
	 * The class name of the response object for the relation, if using DTOs.
	 */
	protected TypeName relationResponseType;

	/**
	 * The class name of the related entity.
	 */
	protected TypeName relationEntityType;

	/**
	 * The class name of the related entity's ID.
	 */
	protected TypeName relationIdType;

	/**
	 * The name of the method to access the ID of the related entity/DTO.
	 */
	protected String relationIdAccessor;

	/**
	 * The name of the method used to remove the relation from the main entity.
	 */
	protected String relationRemover;

	/**
	 * Static factory method to create a new instance of {@link DeleteFromRelationsMethodBuilder}.
	 *
	 * @return A new instance of {@link DeleteFromRelationsMethodBuilder}.
	 */
	public static DeleteFromRelationsMethodBuilder create() {
		return new DeleteFromRelationsMethodBuilder();
	}

	/**
	 * Configures the builder with general controller settings.
	 *
	 * @param configuration The controller configuration.
	 * @return The builder instance for chaining.
	 */
	public DeleteFromRelationsMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
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
	public DeleteFromRelationsMethodBuilder withRelation(@NotNull RelationConfiguration relation) {
		return this.withRelationName(relation.getRelationName())
				.withUsingDto(relation.isUsingDto())
				.withRelationResponseType(relation.getResponseObjectType())
				.withRelationEntityType(relation.getEntityType())
				.withRelationIdType(relation.getIdType())
				.withRelationIdAccessor(relation.getIdAccessorMethodName())
				.withRelationRemover(relation.getRemoverMethodName());
	}

	/**
	 * Adds the generated "delete from relation" methods (by object and by ID)
	 * to the provided {@link TypeSpec.Builder}, if they don't already exist.
	 *
	 * @param builder The {@link TypeSpec.Builder} for the controller class.
	 * @return The modified {@link TypeSpec.Builder}.
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		TypeName responseEntityType = this.isUsingDto && this.relationResponseType != null && !this.relationResponseType.equals(TypeName.OBJECT) ? this.relationResponseType : this.relationEntityType;
		// DELETE by ID typically returns 204 No Content
		TypeName responseTypeById = ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), TypeName.OBJECT);
		// DELETE by DTO might delegate or return something else, but let's assume it delegates to ById for now.
		// If it were to return the deleted item (less common for DELETE), the type would be needed.
		// For delegation, the return type of the *delegating* method matters. Let's assume it also returns NoContent via delegation.
		TypeName responseTypeByDto = responseTypeById; // Assuming delegation to ById which returns Void/NoContent

		if (!this.hasExistingRequestById()) {
			addDeleteFromRelationsByIdMethod(builder, responseTypeById);
		}

		if (!this.hasExistingRequest()) {
			addDeleteFromRelationsMethod(builder, responseEntityType, responseTypeByDto);
		}

		return builder;
	}

	/**
	 * Generates and adds the method for deleting a related entity by its ID.
	 * The method retrieves the main entity, retrieves a reference to the related entity by ID,
	 * removes the related entity from the main entity's collection, updates the main entity,
	 * and returns 204 No Content.
	 *
	 * @param builder          The {@link TypeSpec.Builder} for the controller class.
	 * @param responseTypeById The {@link TypeName} for the response (typically ResponseEntity<Void>).
	 */
	private void addDeleteFromRelationsByIdMethod(TypeSpec.Builder builder, TypeName responseTypeById) {
		log.info("addDeleteFromRelationsByIdMethod");
		String methodName = RestnessUtil.getRelationMethodName(this.relationName, AccessorType.REMOVE) + "ById";
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
			mb.addStatement("entity." + this.relationRemover + "(rel)");
			mb.addStatement("this.dataAccessor.updateData(entity)");
			// Injector handles returning NoContent
		};

		// Use a separate injector for NoContent response
		Consumer<MethodSpec.Builder> postBodyConfigurer = mb -> new NoContentStatementInjector()
				.withWrapper(this.responseWrapperType) // Wrapper might influence headers even for NoContent
				.inject(mb);

		MethodSpec methodSpec = buildRelationMethodSpec(methodName, path, responseTypeById, parameterConfigurer, bodyConfigurer, postBodyConfigurer);
		builder.addMethod(methodSpec);
	}

	/**
	 * Generates and adds the method for deleting a related entity using its DTO/entity representation
	 * provided in the request body. This method typically delegates to the "ById" version
	 * after extracting the ID from the request body object.
	 *
	 * @param builder            The {@link TypeSpec.Builder} for the controller class.
	 * @param responseEntityType The {@link TypeName} of the individual entity/DTO used in the request body.
	 * @param responseTypeByDto  The {@link TypeName} for the response (typically ResponseEntity<Void> if delegating).
	 */
	private void addDeleteFromRelationsMethod(TypeSpec.Builder builder, TypeName responseEntityType, TypeName responseTypeByDto) {
		log.info("addDeleteFromRelationsMethod");
		String methodName = RestnessUtil.getRelationMethodName(this.relationName, AccessorType.REMOVE);
		String path = this.basePath + "/{id}/" + this.relationName;

		Consumer<MethodSpec.Builder> parameterConfigurer = mb -> mb.addParameter(
				ParameterSpec.builder(responseEntityType, "dto") // Use the determined type for the request body
						.addAnnotation(RequestBody.class)
						.addAnnotation(Valid.class) // Add validation
						.build()
		);

		// Delegate to the ById method
		Consumer<MethodSpec.Builder> bodyConfigurer = mb -> mb.addStatement(
				"return this." + RestnessUtil.getRelationMethodName(this.relationName, AccessorType.REMOVE) + "ById(id, dto." + this.relationIdAccessor + "())"
		);

		MethodSpec methodSpec = buildRelationMethodSpec(methodName, path, responseTypeByDto, parameterConfigurer, bodyConfigurer, null); // No post-body configurer needed for simple delegation
		builder.addMethod(methodSpec);
	}

	/**
	 * Helper method to build the common structure of the {@link MethodSpec} for
	 * the "delete from relation" methods. This includes annotations, modifiers,
	 * the main entity ID path variable, security injection, and return type definition.
	 *
	 * @param methodName          The name for the generated method.
	 * @param path                The request mapping path for the method.
	 * @param returnType          The {@link TypeName} for the method's return value (e.g., ResponseEntity<Void>).
	 * @param parameterConfigurer A {@link Consumer} to add specific parameters (like relationId or request body).
	 * @param bodyConfigurer      A {@link Consumer} to add the main method body statements.
	 * @param postBodyConfigurer  A {@link Consumer} (nullable) to add statements after the main body (e.g., for response generation).
	 * @return The constructed {@link MethodSpec}.
	 */
	private MethodSpec buildRelationMethodSpec(String methodName, String path, TypeName returnType, Consumer<MethodSpec.Builder> parameterConfigurer, Consumer<MethodSpec.Builder> bodyConfigurer, Consumer<MethodSpec.Builder> postBodyConfigurer) {
		MethodSpec.Builder methodBuilder = MethodSpec
				.methodBuilder(methodName)
				.addAnnotation(AnnotationSpec.builder(DeleteMapping.class) // Use DeleteMapping
						.addMember("value", "$S", path)
						.build())
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
				.withMethod("UPDATE") // Removing from a relation modifies the parent, often treated as UPDATE permission-wise. Could be DELETE.
				.withEntityClassName(this.entityType)
				.withRelatedClassName(this.relationEntityType)
				.withSecurityConfig(this.securityConfig)
				.inject(methodBuilder);

		// Set the return type (already determined before calling this method)
		methodBuilder.returns(returnType);

		// Add the main method body logic
		bodyConfigurer.accept(methodBuilder);

		// Add any post-body logic (like response generation)
		if (postBodyConfigurer != null) {
			postBodyConfigurer.accept(methodBuilder);
		}

		return methodBuilder.build();
	}

	/**
	 * Checks if a request mapping for deleting a relation via request body
	 * (DELETE on /base/{id}/relationName) already exists.
	 *
	 * @return {@code true} if a matching mapping exists, {@code false} otherwise.
	 */
	protected boolean hasExistingRequest() {
		return RestnessUtil.hasExistingRequest(
				this.requestMappings,
				this.basePath + "/{id}/" + this.relationName,
				RequestMethod.DELETE
		);
	}

	/**
	 * Checks if a request mapping for deleting a relation via relation ID
	 * (DELETE on /base/{id}/relationName/{relationId}) already exists.
	 *
	 * @return {@code true} if a matching mapping exists, {@code false} otherwise.
	 */
	protected boolean hasExistingRequestById() {
		return RestnessUtil.hasExistingRequest(
				this.requestMappings,
				this.basePath + "/{id}/" + this.relationName + "/{relationId}",
				RequestMethod.DELETE
		);
	}
}