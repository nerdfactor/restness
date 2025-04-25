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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder class responsible for generating the method that deletes (sets to null)
 * a single relation of a main entity within a REST controller.
 * The method identifies the main entity using its ID from the path variable,
 * sets the relation property to null, updates the main entity,
 * and returns a No Content response.
 */
@Slf4j
@With
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class DeleteSingleRelationMethodBuilder extends MethodBuilder {

	/**
	 * List of existing request mappings in the controller to avoid duplicates.
	 */
	protected List<String> existingRequestMappings;

	/**
	 * The base path for the controller's request mappings.
	 */
	protected String requestBasePath;

	/**
	 * The {@link TypeName} of the entity's identifier.
	 */
	protected TypeName idClassName;

	/**
	 * The {@link TypeName} of the main entity managed by the controller.
	 */
	protected TypeName entityClassName;

	/**
	 * The security configuration for the controller.
	 */
	protected SecurityConfiguration securityConfiguration;

	/**
	 * The {@link TypeName} of the wrapper class used for responses, if any.
	 */
	protected TypeName responseWrapperClassName;

	/**
	 * The name of the relation property in the entity.
	 */
	protected String relationName;

	/**
	 * The {@link TypeName} of the entity class on the other side of the relation.
	 */
	protected TypeName relationEntityClassName;

	/**
	 * The name of the setter method for the relation property in the main entity.
	 */
	protected String relationSetterMethodName;

	/**
	 * Static factory method to create a new instance of {@link DeleteSingleRelationMethodBuilder}.
	 *
	 * @return A new instance of {@link DeleteSingleRelationMethodBuilder}.
	 */
	public static DeleteSingleRelationMethodBuilder create() {
		return new DeleteSingleRelationMethodBuilder();
	}

	/**
	 * Sets the {@link RelationConfiguration} to be used for building the method
	 * and extracts relevant properties into fields.
	 *
	 * @param relation The {@link RelationConfiguration}.
	 * @return The current {@link DeleteSingleRelationMethodBuilder} instance.
	 */
	public DeleteSingleRelationMethodBuilder withRelation(RelationConfiguration relation) {
		return this.withRelationName(relation.getRelationName())
				.withRelationEntityClassName(relation.getEntityClassName())
				.withRelationSetterMethodName(relation.getSetterMethodName());
	}

	/**
	 * Sets the {@link ControllerConfiguration} and extracts relevant properties into fields.
	 *
	 * @param configuration The {@link ControllerConfiguration}.
	 * @return The current {@link DeleteSingleRelationMethodBuilder} instance.
	 */
	@Override
	public DeleteSingleRelationMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		// Store individual configuration properties instead of the whole object
		return this.withExistingRequestMappings(configuration.getExistingRequestMappings())
				.withRequestBasePath(configuration.getRequestBasePath())
				.withIdClassName(configuration.getIdClassName())
				.withEntityClassName(configuration.getEntityClassName())
				.withSecurityConfiguration(configuration.getSecurityConfiguration())
				.withResponseWrapperClassName(configuration.getResponseWrapperClassName());
	}

	/**
	 * Adds the generated "delete single relation" method to the provided
	 * {@link TypeSpec.Builder}, if it doesn't already exist.
	 *
	 * @param builder The {@link TypeSpec.Builder} for the controller class.
	 * @return The modified {@link TypeSpec.Builder}.
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		if (this.hasExistingRequest()) {
			log.debug("Skipping delete single relation method for {} as it already exists.", this.relationName);
			return builder;
		}
		addDeleteSingleRelationMethod(builder);
		return builder;
	}

	/**
	 * Generates and adds the method for deleting a single relation.
	 * The method retrieves the main entity, sets the relation to null,
	 * updates the entity, and returns a No Content response.
	 *
	 * @param builder The {@link TypeSpec.Builder} for the controller class.
	 */
	private void addDeleteSingleRelationMethod(TypeSpec.Builder builder) {
		log.info("Adding delete single relation method for relation: {}", this.relationName);

		String methodName = RestnessUtil.getRelationMethodName(this.relationName, AccessorType.REMOVE);
		String path = this.requestBasePath + "/{id}/" + this.relationName;

		// Define parameter configuration (just the ID path variable)
		Consumer<MethodSpec.Builder> parameterConfigurer = mb -> mb.addParameter(
				ParameterSpec.builder(this.idClassName, "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
		);

		// Define body configuration
		Consumer<MethodSpec.Builder> bodyConfigurer = mb -> {
			mb.addStatement("$T entity = this.dataAccessor.readData(id).orElseThrow($T::new)", this.entityClassName, EntityNotFoundException.class);
			mb.addStatement("entity." + this.relationSetterMethodName + "(null)");
			mb.addStatement("this.dataAccessor.updateData(entity)");
		};

		// Define post-body configuration (return no content)
		Consumer<MethodSpec.Builder> postBodyConfigurer = mb -> new NoContentStatementInjector()
				.withWrapper(this.responseWrapperClassName)
				.inject(mb);

		MethodSpec methodSpec = buildRelationMethodSpec(methodName, path, parameterConfigurer, bodyConfigurer, postBodyConfigurer);
		builder.addMethod(methodSpec);
	}

	/**
	 * Helper method to build the common structure of the {@link MethodSpec} for
	 * the "delete single relation" method. This includes annotations, modifiers,
	 * the main entity ID path variable, security injection, and return type definition.
	 *
	 * @param methodName          The name for the generated method.
	 * @param path                The request mapping path for the method.
	 * @param parameterConfigurer A {@link Consumer} to add specific parameters.
	 * @param bodyConfigurer      A {@link Consumer} to add the main method body statements.
	 * @param postBodyConfigurer  A {@link Consumer} to add statements after the main body (for response generation).
	 * @return The constructed {@link MethodSpec}.
	 */
	private MethodSpec buildRelationMethodSpec(String methodName, String path, Consumer<MethodSpec.Builder> parameterConfigurer, Consumer<MethodSpec.Builder> bodyConfigurer, Consumer<MethodSpec.Builder> postBodyConfigurer) {
		MethodSpec.Builder methodBuilder = MethodSpec
				.methodBuilder(methodName)
				.addAnnotation(AnnotationSpec.builder(DeleteMapping.class)
						.addMember("value", "$S", path)
						.build())
				.addAnnotation(ResponseBody.class) // Keep for consistency
				.addModifiers(Modifier.PUBLIC);

		// Add parameters (ID path variable)
		parameterConfigurer.accept(methodBuilder);

		// Inject security checks
		methodBuilder = new AuthenticationInjector()
				.withMethod("UPDATE") // Deleting (setting to null) requires UPDATE permission
				.withEntityClassName(this.entityClassName)
				.withRelatedClassName(this.relationEntityClassName) // Check based on the related entity
				.withSecurityConfig(this.securityConfiguration)
				.inject(methodBuilder);

		// Define return type (ResponseEntity<?>)
		// NoContentStatementInjector handles the actual return value, but the signature needs to be compatible.
		TypeName returnType = ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), WildcardTypeName.subtypeOf(Object.class));
		methodBuilder.returns(returnType);

		// Add the main method body logic
		bodyConfigurer.accept(methodBuilder);

		// Add post-body logic (response generation)
		if (postBodyConfigurer != null) {
			postBodyConfigurer.accept(methodBuilder);
		}

		return methodBuilder.build();
	}

	/**
	 * Checks if a request mapping for deleting a single relation
	 * (DELETE on /base/{id}/relationName) already exists.
	 *
	 * @return {@code true} if a matching mapping exists, {@code false} otherwise.
	 */
	protected boolean hasExistingRequest() {
		return RestnessUtil.hasExistingRequest(
				this.existingRequestMappings,
				this.requestBasePath + "/{id}/" + this.relationName,
				RequestMethod.DELETE
		);
	}
}