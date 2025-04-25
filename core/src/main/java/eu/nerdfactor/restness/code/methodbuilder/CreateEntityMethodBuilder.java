package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.builder.Buildable;
import eu.nerdfactor.restness.code.builder.Configurable;
import eu.nerdfactor.restness.code.injector.AuthenticationInjector;
import eu.nerdfactor.restness.code.injector.ReturnStatementInjector;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.config.SecurityConfiguration;
import eu.nerdfactor.restness.util.RestnessUtil;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Modifier;

/**
 * Builder for creating the method that creates a new entity in a REST controller.
 * This builder is responsible for generating the {@code create(dto)} method, typically annotated
 * with {@code @PostMapping}. It handles checking for existing methods, injecting
 * authentication logic, defining the method body for creating the entity, mapping
 * DTOs if necessary, and wrapping the response.
 *
 * @author Daniel Klug
 */
@Slf4j
@With
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateEntityMethodBuilder implements Buildable<TypeSpec.Builder>, Configurable<ControllerConfiguration> {

	/**
	 * Flag indicating if a request mapping for creating an entity already exists.
	 */
	protected boolean hasExistingRequest;
	/**
	 * The URL path for the create entity request (e.g., "/entities").
	 */
	protected String requestUrl;
	/**
	 * The {@link TypeName} of the request body object (DTO or entity).
	 */
	protected TypeName requestType;
	/**
	 * The {@link TypeName} of the response object (DTO or entity).
	 */
	protected TypeName responseType;
	/**
	 * The {@link TypeName} of the entity being created.
	 */
	protected TypeName entityType;
	/**
	 * Flag indicating if Data Transfer Objects (DTOs) are used for request/response.
	 */
	protected boolean isUsingDto;
	/**
	 * Security configuration for the controller method.
	 */
	protected SecurityConfiguration securityConfiguration;
	/**
	 * The {@link TypeName} of the class used to wrap the response data, if any.
	 */
	protected TypeName dataWrapperClass;

	/**
	 * Creates a new instance of {@link CreateEntityMethodBuilder}.
	 *
	 * @return A new {@link CreateEntityMethodBuilder}.
	 */
	public static CreateEntityMethodBuilder create() {
		return new CreateEntityMethodBuilder();
	}

	/**
	 * Configures the builder with the provided {@link ControllerConfiguration}.
	 * It extracts necessary information like request paths, types, DTO usage,
	 * security settings, and response wrapping.
	 *
	 * @param configuration The {@link ControllerConfiguration} for the controller.
	 * @return A new configured instance of {@link CreateEntityMethodBuilder}.
	 */
	@Override
	public CreateEntityMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		return this.withHasExistingRequest(RestnessUtil.hasExistingRequest(configuration.getExistingRequestMappings(), configuration.getRequestBasePath(), RequestMethod.POST))
				.withRequestUrl(configuration.getRequestBasePath())
				.withRequestType(configuration.getRequestType())
				.withResponseType(configuration.getResponseType())
				.withEntityType(configuration.getEntityClassName())
				.withUsingDto(configuration.isUsingDto())
				.withSecurityConfiguration(configuration.getSecurityConfiguration())
				.withDataWrapperClass(configuration.getResponseWrapperClassName());
	}

	/**
	 * Builds the create entity method and adds it to the provided {@link TypeSpec.Builder}.
	 * If a method with the same signature already exists, it skips the generation.
	 * Otherwise, it creates the method declaration, injects authentication, adds the
	 * method body (including DTO mapping and data access logic), and injects the
	 * return statement logic.
	 *
	 * @param builder The {@link TypeSpec.Builder} for the controller class.
	 * @return The updated {@link TypeSpec.Builder} with the new method added (if applicable).
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		// Check, if the controller already contains a Post method with the Request Url.
		if (this.hasExistingRequest) {
			return builder;
		}
		log.info("addCreateEntityMethod");

		// Create the method declaration.
		MethodSpec.Builder method = this.createMethodDeclaration(this.requestUrl, this.requestType, this.responseType);

		// Inject a Security Annotation that will require a role of "CREATE"
		// for the Entity.
		new AuthenticationInjector().withMethod("CREATE")
				.withEntityClassName(this.entityType)
				.withSecurityConfig(this.securityConfiguration)
				.inject(method);

		// Add the method body.
		this.addMethodBody(method, this.entityType, this.requestType, this.responseType, this.isUsingDto);

		// Inject a return statement that will return the response object in a ResponseEntity
		// that may be wrapped inside the DataWrapper.
		new ReturnStatementInjector()
				.withWrapper(this.dataWrapperClass)
				.withResponse(this.responseType)
				.inject(method);

		builder.addMethod(method.build());
		return builder;
	}

	/**
	 * Create a Post method called "create" with the requestUrl that takes a Valid
	 * object of requestType from the RequestBody (called "dto") and will return an
	 * ResponseEntity with an object of responseType.
	 *
	 * @param requestUrl   The requested Url.
	 * @param requestType  The type of object inside the RequestBody.
	 * @param responseType The type of object of the response.
	 * @return The {@link MethodSpec.Builder} of the new method declaration.
	 */
	protected MethodSpec.Builder createMethodDeclaration(String requestUrl, TypeName requestType, TypeName responseType) {
		return MethodSpec.methodBuilder("create")
				.addAnnotation(AnnotationSpec.builder(PostMapping.class).addMember("value", "$S", requestUrl).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseType))
				.addParameter(ParameterSpec.builder(requestType, "dto").addAnnotation(RequestBody.class).addAnnotation(Valid.class).build());
	}

	/**
	 * Add a method body that creates a new Entity from the object in the RequestBody
	 * with the help of the DataAccessor and return the result. Handles potential DTO mapping.
	 *
	 * @param method       The existing {@link MethodSpec.Builder}.
	 * @param entityType   The type of the Entity.
	 * @param requestType  The type of object inside the RequestBody.
	 * @param responseType The type of object of the response.
	 * @param isUsingDto   If the method is using DTOs.
	 */
	protected void addMethodBody(MethodSpec.Builder method, TypeName entityType, TypeName requestType, TypeName responseType, boolean isUsingDto) {
		// If the method is using DTOs, the object from the RequestBody will
		// be mapped into the type of the Entity.
		if (isUsingDto) {
			method.addStatement("$T created = this.dataMapper.map(dto, $T.class)", entityType, entityType);
		} else {
			method.addStatement("$T created = dto", requestType);
		}

		// Create the new Entity with help of the DataAccessor.
		method.addStatement("created = this.dataAccessor.createData(created)");

		// If the method is using DTOs, the created object will be mapped into
		// the responseType. Otherwise, the responseType is equals to the entityType
		// and does not need to be mapped.
		if (isUsingDto) {
			method.addStatement("$T response = this.dataMapper.map(created, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = created", responseType);
		}
	}
}
