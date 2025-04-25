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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Modifier;

/**
 * Builder for creating the method that updates an existing entity in a REST controller.
 * This builder is responsible for generating the {@code update(id, dto)} method, typically
 * annotated with {@code @PatchMapping("/{id}")}. It handles checking for existing methods,
 * injecting authentication logic, defining the method body for retrieving the entity,
 * merging changes from the request DTO, saving the updated entity, mapping DTOs if
 * necessary, and wrapping the response.
 *
 * @author Daniel Klug
 */
@Slf4j
@With
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateEntityMethodBuilder implements Buildable<TypeSpec.Builder>, Configurable<ControllerConfiguration> {

	/**
	 * Flag indicating if a request mapping for updating an entity already exists.
	 */
	protected boolean hasExistingRequest;
	/**
	 * The URL path for the update entity request (e.g., "/entities/{id}").
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
	 * The {@link TypeName} of the entity being updated.
	 */
	protected TypeName entityType;
	/**
	 * The {@link TypeName} of the entity's identifier (e.g., Long, String).
	 */
	protected TypeName identifyingType;
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
	 * Creates a new instance of {@link UpdateEntityMethodBuilder}.
	 *
	 * @return A new {@link UpdateEntityMethodBuilder}.
	 */
	public static UpdateEntityMethodBuilder create() {
		return new UpdateEntityMethodBuilder();
	}

	/**
	 * Configures the builder with the provided {@link ControllerConfiguration}.
	 * It extracts necessary information like request paths, types, DTO usage,
	 * security settings, and response wrapping.
	 *
	 * @param configuration The {@link ControllerConfiguration} for the controller.
	 * @return A new configured instance of {@link UpdateEntityMethodBuilder}.
	 */
	@Override
	public UpdateEntityMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		return this.withHasExistingRequest(RestnessUtil.hasExistingRequest(configuration.getExistingRequestMappings(), configuration.getRequestBasePath() + "/{id}", RequestMethod.PATCH))
				.withRequestUrl(configuration.getRequestBasePath() + "/{id}")
				.withRequestType(configuration.getRequestType())
				.withResponseType(configuration.getResponseType())
				.withEntityType(configuration.getEntityClassName())
				.withIdentifyingType(configuration.getIdClassName())
				.withUsingDto(configuration.isUsingDto())
				.withSecurityConfiguration(configuration.getSecurityConfiguration())
				.withDataWrapperClass(configuration.getResponseWrapperClassName());
	}

	/**
	 * Builds the update entity method and adds it to the provided {@link TypeSpec.Builder}.
	 * If a method with the same signature already exists, it skips the generation.
	 * Otherwise, it creates the method declaration, injects authentication, adds the
	 * method body (including entity retrieval, merging, saving, and DTO mapping),
	 * and injects the return statement logic.
	 *
	 * @param builder The {@link TypeSpec.Builder} for the controller class.
	 * @return The updated {@link TypeSpec.Builder} with the new method added (if applicable).
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		if (this.hasExistingRequest) {
			return builder;
		}
		log.info("addUpdateEntityMethod");

		MethodSpec.Builder method = this.createMethodDeclaration(this.requestUrl, this.identifyingType, this.responseType, this.requestType);

		new AuthenticationInjector()
				.withMethod("UPDATE")
				.withEntityClassName(this.entityType)
				.withSecurityConfig(this.securityConfiguration)
				.inject(method);

		this.addMethodBody(method, this.entityType, this.responseType, this.isUsingDto);

		method = new ReturnStatementInjector()
				.withWrapper(this.dataWrapperClass)
				.withResponse(this.responseType)
				.inject(method);

		builder.addMethod(method.build());
		return builder;
	}

	/**
	 * Create a Path method called "update" with the requestUrl that takes a Valid
	 * object of requestType from the RequestBody (called "dto") and will return an
	 * ResponseEntity with an object of responseType.
	 *
	 * @param requestUrl      The requested Url.
	 * @param identifyingType The type of object identifying the Entity.
	 * @param responseType    The type of object of the response.
	 * @return The {@link MethodSpec.Builder} of the new method declaration.
	 */
	protected MethodSpec.Builder createMethodDeclaration(String requestUrl, TypeName identifyingType, TypeName responseType, TypeName requestType) {
		return MethodSpec.methodBuilder("update")
				.addAnnotation(AnnotationSpec.builder(PatchMapping.class).addMember("value", "$S", requestUrl).build())
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
	 * DataAccessor and the provided id and updates it with the object
	 * in the RequestBody with the help of the DataMerger and saves
	 * the changes with the help of the DataAccessor and return the result.
	 * Will throw a new EntityNotFoundException if no Entity could be
	 * found.
	 *
	 * @param method       The existing {@link MethodSpec.Builder}.
	 * @param entityType   The type of the Entity.
	 * @param responseType The type of object of the response.
	 * @param isUsingDto   If the method is using DTOs.
	 */
	protected void addMethodBody(MethodSpec.Builder method, TypeName entityType, TypeName responseType, boolean isUsingDto) {
		method.addStatement("$T entity = this.dataAccessor.readData(id).orElseThrow($T::new)", entityType, EntityNotFoundException.class);
		if (isUsingDto) {
			method.addStatement("$T changed = this.dataMapper.map(dto, $T.class)", entityType, entityType);
		} else {
			method.addStatement("$T changed = dto", entityType);
		}
		method.addStatement("$T updated = this.dataMerger.merge(entity, changed)", entityType);
		method.addStatement("updated = this.dataAccessor.updateData(updated)");
		if (isUsingDto) {
			method.addStatement("$T response = this.dataMapper.map(updated, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = updated", responseType);
		}
	}
}
