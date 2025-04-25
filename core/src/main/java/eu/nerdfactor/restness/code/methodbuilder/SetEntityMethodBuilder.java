package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.builder.Buildable;
import eu.nerdfactor.restness.code.builder.Configurable;
import eu.nerdfactor.restness.code.injector.AuthenticationInjector;
import eu.nerdfactor.restness.code.injector.ReturnStatementInjector;
import eu.nerdfactor.restness.config.ControllerConfiguration;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Modifier;

/**
 * Builder for creating the method that updates an existing entity by its id in a REST controller.
 * This builder is responsible for generating the {@code set(id, dto)} method, typically annotated
 * with {@code @PutMapping("/{id}")}. It handles checking for existing methods, injecting
 * authentication logic, defining the method body for retrieving and updating the entity,
 * handling DTO mapping, and wrapping the response.
 */
@Slf4j
@With
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SetEntityMethodBuilder implements Buildable<TypeSpec.Builder>, Configurable<ControllerConfiguration> {

	/**
	 * Flag indicating if a request mapping for updating an entity already exists.
	 */
	protected boolean hasExistingRequest;
	/**
	 * The URL path for the update entity request (e.g., "/{id}").
	 */
	protected String requestUrl;
	/**
	 * The {@link TypeName} of the request object (DTO or entity).
	 */
	protected TypeName requestType;
	/**
	 * The {@link TypeName} of the response object (DTO or entity).
	 */
	protected TypeName responseType;
	/**
	 * The {@link TypeName} of the entity being updated.
	 */
	protected TypeName entityType;
	/**
	 * The {@link TypeName} of the entity's identifier (e.g., Long, String).
	 */
	protected TypeName identifyingType;
	/**
	 * Flag indicating if Data Transfer Objects (DTOs) are used for request and response.
	 */
	protected boolean isUsingDto;
	/**
	 * Security configuration for the controller method.
	 */
	protected SecurityConfiguration securityConfiguration;
	/**
	 * The {@link TypeName} of the class used to wrap the response data, if any.
	 */
	protected TypeName dataWrapperType;

	/**
	 * Creates a new instance of {@link SetEntityMethodBuilder}.
	 *
	 * @return A new {@link SetEntityMethodBuilder}.
	 */
	public static SetEntityMethodBuilder create() {
		return new SetEntityMethodBuilder();
	}

	/**
	 * Configures the builder with the provided {@link ControllerConfiguration}.
	 * It extracts necessary information like request paths, types, security settings,
	 * and checks for existing PUT mappings.
	 *
	 * @param configuration The {@link ControllerConfiguration} for the controller.
	 * @return A new configured instance of {@link SetEntityMethodBuilder}.
	 */
	@Override
	public SetEntityMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		return this.withHasExistingRequest(RestnessUtil.hasExistingRequest(configuration.getExistingRequestMappings(), configuration.getRequestBasePath() + "/{id}", RequestMethod.PUT))
				.withRequestUrl(configuration.getRequestBasePath() + "/{id}")
				.withRequestType(configuration.getRequestType())
				.withResponseType(configuration.getResponseType())
				.withEntityType(configuration.getEntityClassName())
				.withIdentifyingType(configuration.getIdClassName())
				.withUsingDto(configuration.isUsingDto())
				.withSecurityConfiguration(configuration.getSecurityConfiguration())
				.withDataWrapperType(configuration.getResponseWrapperClassName());
	}

	/**
	 * Builds the set entity method and adds it to the provided {@link TypeSpec.Builder}.
	 * If a method with the same signature already exists, it skips the generation.
	 * Otherwise, it creates the method declaration, injects authentication, adds the
	 * method body (including entity retrieval, mapping, and update), and injects the
	 * return statement logic.
	 *
	 * @param builder The {@link TypeSpec.Builder} for the controller class.
	 * @return The updated {@link TypeSpec.Builder} with the new method added (if applicable).
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		// Check, if the controller already contains a Put method with the Request Url, an id parameter and a request body.
		if (this.hasExistingRequest) {
			return builder;
		}
		log.info("addSetEntityMethod");

		MethodSpec.Builder method = this.createMethodDeclaration(this.requestUrl, this.identifyingType, this.responseType, this.requestType);

		new AuthenticationInjector()
				.withMethod("UPDATE")
				.withEntityClassName(this.entityType)
				.withSecurityConfig(this.securityConfiguration)
				.inject(method);

		this.addMethodBody(method, this.entityType, this.responseType, this.isUsingDto);

		new ReturnStatementInjector()
				.withWrapper(this.dataWrapperType)
				.withResponse(this.responseType) // Use responseType directly
				.inject(method);
		builder.addMethod(method.build());
		return builder;
	}

	/**
	 * Create a Put method called "set" with the requestUrl that takes a Valid
	 * object of requestType from the RequestBody (called "dto") and an identifyingType
	 * (called "id") from the PathVariable. It will return a ResponseEntity with an
	 * object of responseType.
	 *
	 * @param requestUrl      The requested Url.
	 * @param identifyingType The type of object identifying the Entity.
	 * @param responseType    The type of object of the response.
	 * @param requestType     The type of object of the request body.
	 * @return The {@link MethodSpec.Builder} of the new method declaration.
	 */
	protected MethodSpec.Builder createMethodDeclaration(String requestUrl, TypeName identifyingType, TypeName responseType, TypeName requestType) {
		return MethodSpec.methodBuilder("set")
				.addAnnotation(AnnotationSpec.builder(PutMapping.class).addMember("value", "$S", requestUrl).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseType))
				.addParameter(ParameterSpec.builder(identifyingType, "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				)
				.addParameter(ParameterSpec.builder(requestType, "dto")
						.addAnnotation(RequestBody.class)
						.addAnnotation(Valid.class)
						.build()
				);
	}

	/**
	 * Add a method body that finds an Entity with the help of the
	 * DataAccessor and the provided id, maps the request DTO to the entity
	 * (if applicable), updates the entity using the DataAccessor, maps the
	 * updated entity back to a response DTO (if applicable), and prepares
	 * the response variable. Will throw a new EntityNotFoundException if no
	 * Entity could be found.
	 *
	 * @param method       The existing {@link MethodSpec.Builder}.
	 * @param entityType   The type of the Entity.
	 * @param responseType The type of object of the response.
	 * @param isUsingDto   If the method is using DTOs for request/response.
	 */
	protected void addMethodBody(MethodSpec.Builder method, TypeName entityType, TypeName responseType, boolean isUsingDto) {
		method.addStatement("$T entity = this.dataAccessor.readData(id).orElseThrow($T::new)", entityType, EntityNotFoundException.class);
		if (isUsingDto) {
			// Assuming dataMapper maps from requestType (dto) to entityType
			method.addStatement("$T changed = this.dataMapper.map(dto, $T.class)", entityType, entityType);
			// Ensure the ID from the path is used, not potentially from the DTO
			// This requires the entity to have a setId method or similar mechanism.
			// If direct field access or a different setter name is needed, adjust accordingly.
			// Consider adding a check if the entity has an ID setter.
			method.addStatement("changed.setId(id)"); // Or appropriate setter
		} else {
			method.addStatement("$T changed = dto", entityType);
			// Ensure the ID from the path is used even if not using DTOs
			method.addStatement("changed.setId(id)"); // Or appropriate setter
		}
		method.addStatement("changed = this.dataAccessor.updateData(changed)");
		if (isUsingDto) {
			method.addStatement("$T response = this.dataMapper.map(changed, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = changed", responseType);
		}
	}
}