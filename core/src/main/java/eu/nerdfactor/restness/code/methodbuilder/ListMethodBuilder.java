package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.injector.AuthenticationInjector;
import eu.nerdfactor.restness.code.injector.ReturnStatementInjector;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.config.SecurityConfiguration;
import eu.nerdfactor.restness.util.RestnessUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * A builder that can be used to create a list method in a controller.
 * This builder is responsible for generating the {@code all()} method,
 * typically annotated with {@code @GetMapping}. It handles checking for
 * existing methods, injecting authentication logic, defining the method
 * body for retrieving all entities, handling DTO mapping, and wrapping
 * the response list.
 *
 * @author Daniel Klug
 */
@Slf4j
@With
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ListMethodBuilder extends MethodBuilder {

	/**
	 * Flag indicating if a request mapping for listing entities (GET) already exists.
	 */
	protected boolean requestExists;

	/**
	 * The base path for the generated request mapping (e.g., "/entities").
	 */
	private String basePath;

	/**
	 * The {@link TypeName} of the response object (DTO or entity) contained in the list.
	 */
	private TypeName responseBodyType;

	/**
	 * The {@link TypeName} of the entity being listed.
	 */
	private TypeName entityType;

	/**
	 * Security configuration for the controller method.
	 */
	private SecurityConfiguration securityConfig;

	/**
	 * Flag indicating if Data Transfer Objects (DTOs) are used for the response.
	 */
	private boolean isUsingDto;

	/**
	 * The {@link TypeName} of the class used to wrap the response list, if any.
	 */
	private TypeName responseWrapperType;

	/**
	 * Create a new {@link ListMethodBuilder}.
	 *
	 * @return A new {@link ListMethodBuilder}.
	 */
	public static ListMethodBuilder create() {
		return new ListMethodBuilder();
	}

	/**
	 * Configures the builder with the provided {@link ControllerConfiguration}.
	 * It extracts necessary information like request paths, types, security settings,
	 * and checks for existing GET mappings.
	 *
	 * @param configuration The {@link ControllerConfiguration} for the controller.
	 * @return A new configured instance of {@link ListMethodBuilder}.
	 */
	public ListMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		return this.withRequestExists(RestnessUtil.hasExistingRequest(configuration.getExistingRequestMappings(), configuration.getRequestBasePath(), RequestMethod.GET))
				.withBasePath(configuration.getRequestBasePath())
				.withResponseBodyType(configuration.getResponseType())
				.withEntityType(configuration.getEntityClassName())
				.withSecurityConfig(configuration.getSecurityConfiguration())
				.withUsingDto(configuration.isUsingDto())
				.withResponseWrapperType(configuration.getResponseWrapperClassName());
	}

	/**
	 * Builds the list entities method and adds it to the provided {@link TypeSpec.Builder}.
	 * If a method with the same signature already exists, it skips the generation.
	 * Otherwise, it creates the method declaration, injects authentication, adds the
	 * method body (including entity retrieval, mapping, and list creation), and injects the
	 * return statement logic.
	 *
	 * @param builder An existing builder object that will be used.
	 * @return The updated {@link TypeSpec.Builder} with the new method added (if applicable).
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		if (this.requestExists) {
			return builder;
		}
		log.info("addGetAllEntitiesMethod");
		ParameterizedTypeName responseList = ParameterizedTypeName.get(ClassName.get(List.class), this.responseBodyType);
		MethodSpec.Builder method = MethodSpec
				.methodBuilder("all")
				.addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "$S", this.basePath).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseList));
		method = new AuthenticationInjector()
				.withMethod("READ")
				.withEntityClassName(this.entityType)
				.withSecurityConfig(this.securityConfig)
				.inject(method);
		method.addStatement("$T<$T> responseList = new $T<>()", List.class, this.responseBodyType, ArrayList.class);
		method.beginControlFlow("for($T entity : this.dataAccessor.listData())", this.entityType);
		if (this.isUsingDto) {
			method.addStatement("$T response = this.dataMapper.map(entity, $T.class)", this.responseBodyType, this.responseBodyType);
		} else {
			method.addStatement("$T response = entity", this.responseBodyType);
		}
		method.addStatement("responseList.add(response)");
		method.endControlFlow();
		method = new ReturnStatementInjector()
				.withWrapper(this.responseWrapperType)
				.withResponse(this.responseBodyType) // Note: Injector might need adjustment for List<Type>
				.withResponseVariable("responseList")
				.inject(method);
		builder.addMethod(method.build());
		return builder;
	}
}