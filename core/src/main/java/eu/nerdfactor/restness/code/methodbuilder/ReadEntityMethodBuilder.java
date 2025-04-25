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

/**
 * Builder for creating the method that reads a single entity by its id in a REST controller.
 * This builder is responsible for generating the {@code get(id)} method, typically annotated
 * with {@code @GetMapping("/{id}")}. It handles checking for existing methods, injecting
 * authentication logic, defining the method body for retrieving the entity, and wrapping
 * the response.
 *
 * @author Daniel Klug
 */
@Slf4j
@With
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadEntityMethodBuilder implements Buildable<TypeSpec.Builder>, Configurable<ControllerConfiguration> {

	/**
	 * Flag indicating if a request mapping for reading a single entity already exists.
	 */
	protected boolean requestExists;
	/**
	 * The URL path for the read entity request (e.g., "/{id}").
	 */
	protected String basePath;
	/**
	 * The {@link TypeName} of the response object (DTO or entity).
	 */
	protected TypeName responseBodyType;
	/**
	 * The {@link TypeName} of the entity being read.
	 */
	protected TypeName entityType;
	/**
	 * The {@link TypeName} of the entity's identifier (e.g., Long, String).
	 */
	protected TypeName idType;
	/**
	 * Flag indicating if Data Transfer Objects (DTOs) are used for the response.
	 */
	protected boolean isUsingDto;
	/**
	 * Security configuration for the controller method.
	 */
	protected SecurityConfiguration securityConfig;
	/**
	 * The {@link TypeName} of the class used to wrap the response data, if any.
	 */
	protected TypeName responseWrapperType;

	/**
	 * Creates a new instance of {@link ReadEntityMethodBuilder}.
	 *
	 * @return A new {@link ReadEntityMethodBuilder}.
	 */
	public static ReadEntityMethodBuilder create() {
		return new ReadEntityMethodBuilder();
	}

	/**
	 * Configures the builder with the provided {@link ControllerConfiguration}.
	 * It extracts necessary information like request paths, types, and security settings.
	 *
	 * @param configuration The {@link ControllerConfiguration} for the controller.
	 * @return A new configured instance of {@link ReadEntityMethodBuilder}.
	 */
	@Override
	public ReadEntityMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		return this.withRequestExists(RestnessUtil.hasExistingRequest(configuration.getExistingRequestMappings(), configuration.getRequestBasePath() + "/{id}", RequestMethod.GET))
				.withBasePath(configuration.getRequestBasePath() + "/{id}")
				.withResponseBodyType(configuration.getResponseType())
				.withEntityType(configuration.getEntityClassName())
				.withIdType(configuration.getIdClassName())
				.withUsingDto(configuration.isUsingDto())
				.withSecurityConfig(configuration.getSecurityConfiguration())
				.withResponseWrapperType(configuration.getResponseWrapperClassName());
	}

	/**
	 * Builds the read entity method and adds it to the provided {@link TypeSpec.Builder}.
	 * If a method with the same signature already exists, it skips the generation.
	 * Otherwise, it creates the method declaration, injects authentication, adds the
	 * method body, and injects the return statement logic.
	 *
	 * @param builder The {@link TypeSpec.Builder} for the controller class.
	 * @return The updated {@link TypeSpec.Builder} with the new method added (if applicable).
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		// Check, if the controller already contains a Get method with the Request Url and an id parameter.
		if (this.requestExists) {
			return builder;
		}
		log.info("addGetEntityMethod");

		MethodSpec.Builder method = this.createMethodDeclaration(this.basePath, this.idType, this.responseBodyType);

		new AuthenticationInjector()
				.withMethod("READ")
				.withEntityClassName(this.entityType)
				.withSecurityConfig(this.securityConfig)
				.inject(method);

		this.addMethodBody(method, this.entityType, this.responseBodyType, this.isUsingDto);

		new ReturnStatementInjector()
				.withWrapper(this.responseWrapperType)
				.withResponse(this.responseBodyType)
				.inject(method);

		builder.addMethod(method.build());
		return builder;
	}

	/**
	 * Create a Get method called "get" with the requestUrl that hat takes
	 * an identifyingType (called "id") from the PathVariable and will return a
	 * ResponseEntity with an object of responseType.
	 *
	 * @param requestUrl      The requested Url.
	 * @param identifyingType The type of object identifying the Entity.
	 * @param responseType    The type of object of the response.
	 * @return The {@link MethodSpec.Builder} of the new method declaration.
	 */
	protected MethodSpec.Builder createMethodDeclaration(String requestUrl, TypeName identifyingType, TypeName responseType) {
		return MethodSpec.methodBuilder("get")
				.addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "$S", requestUrl).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseType))
				.addParameter(ParameterSpec.builder(identifyingType, "id").addModifiers(Modifier.FINAL).addAnnotation(PathVariable.class).build());
	}

	/**
	 * Add a method body that finds an Entity with the help of the
	 * DataAccessor and the provided id and return the result. Will
	 * throw a new EntityNotFoundException if no Entity could be
	 * found. Handles potential DTO mapping.
	 *
	 * @param method       The existing {@link MethodSpec.Builder}.
	 * @param entityType   The type of the Entity.
	 * @param responseType The type of object of the response.
	 * @param isUsingDto   If the method is using DTOs.
	 */
	protected void addMethodBody(MethodSpec.Builder method, TypeName entityType, TypeName responseType, boolean isUsingDto) {
		method.addStatement("$T entity = this.dataAccessor.readData(id).orElseThrow($T::new)", entityType, EntityNotFoundException.class);
		if (isUsingDto) {
			method.addStatement("$T response = this.dataMapper.map(entity, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = entity", responseType);
		}
	}


}
