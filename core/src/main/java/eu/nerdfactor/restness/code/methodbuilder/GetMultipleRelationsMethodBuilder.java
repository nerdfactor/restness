package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.injector.MethodInjectorRegistry;
import eu.nerdfactor.restness.code.injector.MethodContext;
import eu.nerdfactor.restness.code.injector.RelationAuthenticationInjector;
import eu.nerdfactor.restness.code.injector.OpenApiAnnotationInjector;
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
import org.springframework.web.bind.annotation.ResponseBody;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder class responsible for generating the method that retrieves a collection
 * of related entities associated with a main entity within a REST controller.
 * The method identifies the main entity using its ID from the path variable
 * and returns the list of related entities (or DTOs).
 *
 * @author Daniel Klug
 */
@Slf4j
@With
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class GetMultipleRelationsMethodBuilder extends MethodBuilder {

	/**
	 * List of existing request mappings in the controller to avoid duplicates.
	 */
	protected List<String> requestMappings;

	/**
	 * The base request path from the main controller configuration.
	 */
	protected String basePath;

	/**
	 * The class name of the main entity's identifier (e.g., Long, String).
	 */
	protected TypeName idType;

	/**
	 * The class name of the main entity.
	 */
	protected TypeName entityType;

	/**
	 * The security configuration for the controller.
	 */
	protected SecurityConfiguration securityConfig;

	/**
	 * The class name of the response wrapper, if configured.
	 */
	protected TypeName responseWrapperType;

	/**
	 * The name of the relation property or field.
	 */
	protected String relationName;

	/**
	 * Indicates if the relation should use DTOs.
	 */
	protected boolean isUsingDto;

	/**
	 * The class name of the response DTO for the relation, if used.
	 */
	protected TypeName relationResponseType;

	/**
	 * The class name of the related entity.
	 */
	protected TypeName relationEntityType;

	/**
	 * The name of the getter method for the relation in the main entity.
	 */
	protected String relationGetter;

	/**
	 * Flag indicating if OpenAPI annotations should be generated for the method.
	 */
	protected boolean openApi;

	/**
	 * The {@link MethodInjectorRegistry} for applying custom injectors to this method.
	 */
	private MethodInjectorRegistry injectorRegistry;

	/**
	 * Static factory method to create a new instance of {@link GetMultipleRelationsMethodBuilder}.
	 *
	 * @return A new instance of {@link GetMultipleRelationsMethodBuilder}.
	 */
	public static GetMultipleRelationsMethodBuilder create() {
		return new GetMultipleRelationsMethodBuilder();
	}

	/**
	 * Configures the builder with general controller settings.
	 *
	 * @param configuration The controller configuration.
	 * @return The builder instance for chaining.
	 */
	public GetMultipleRelationsMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
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
	 * @param relation The relation configuration.
	 * @return The builder instance for chaining.
	 */
	public GetMultipleRelationsMethodBuilder withRelation(RelationConfiguration relation) {
		return this.withRelationName(relation.getRelationName())
				.withUsingDto(relation.isUsingDto())
				.withRelationResponseType(relation.getResponseObjectType())
				.withRelationEntityType(relation.getEntityType())
				.withRelationGetter(relation.getGetterMethodName());
	}

	/**
	 * Adds the generated "get multiple relations" method to the provided
	 * {@link TypeSpec.Builder}, if it doesn't already exist.
	 *
	 * @param builder The {@link TypeSpec.Builder} for the controller class.
	 * @return The modified {@link TypeSpec.Builder}.
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		if (this.hasExistingRequest()) {
			log.debug("Skipping get multiple relations method for {} as it already exists.", this.relationName);
			return builder;
		}

		addGetMultipleRelationsMethod(builder);
		return builder;
	}

	/**
	 * Generates and adds the method for retrieving multiple related entities.
	 * The method retrieves the main entity, accesses its relation collection,
	 * maps the related entities to DTOs if configured, and returns the list.
	 *
	 * @param builder The {@link TypeSpec.Builder} for the controller class.
	 */
	private void addGetMultipleRelationsMethod(TypeSpec.Builder builder) {
		log.info("addGetMultipleRelationsMethod for relation {}", this.relationName);

		String methodName = RestnessUtil.getRelationMethodName(this.relationName, AccessorType.GET);
		String path = this.basePath + "/{id}/" + this.relationName;

		TypeName responseEntityType = this.isUsingDto && this.relationResponseType != null && !this.relationResponseType.equals(TypeName.OBJECT)
				? this.relationResponseType
				: this.relationEntityType;
		ParameterizedTypeName responseListType = ParameterizedTypeName.get(ClassName.get(List.class), responseEntityType);

		// No specific parameters beyond the main entity ID handled by the common builder.
		Consumer<MethodSpec.Builder> parameterConfigurer = mb -> {
		};

		Consumer<MethodSpec.Builder> bodyConfigurer = mb -> {
			mb.addStatement("$T entity = this.dataAccessor.readData(id).orElseThrow($T::new)", this.entityType, EntityNotFoundException.class);
			mb.addStatement("$T<$T> responseList = new $T<>()", List.class, responseEntityType, ArrayList.class);
			mb.beginControlFlow("for($T rel : entity." + this.relationGetter + "())", this.relationEntityType);
			if (this.isUsingDto) {
				mb.addStatement("$T response = this.dataMapper.map(rel, $T.class)", responseEntityType, responseEntityType);
			} else {
				mb.addStatement("$T response = rel", responseEntityType);
			}
			mb.addStatement("responseList.add(response)");
			mb.endControlFlow();
			// Return statement is handled by the postBodyConfigurer
		};

		Consumer<MethodSpec.Builder> postBodyConfigurer = mb -> new ReturnStatementInjector()
				.withWrapper(this.responseWrapperType)
				.withResponse(responseEntityType) // Injector needs the *element* type
				.withResponseVariable("responseList") // The variable holding the list
				.inject(mb);

		String entityName = RestnessUtil.toClassName(this.entityType).simpleName();
		String relationCapitalized = this.relationName.substring(0, 1).toUpperCase() + this.relationName.substring(1);

		Consumer<MethodSpec.Builder> openApiConfigurer = mb -> new OpenApiAnnotationInjector()
				.withEnabled(this.openApi)
				.withOperationSummary("List " + relationCapitalized + "s of " + entityName)
				.withOperationId("list" + entityName + relationCapitalized + "s")
				.withResponseCode("200")
				.withResponseDescription("List of related " + relationCapitalized + " entities")
				.addErrorResponse("404", entityName + " not found")
				.inject(mb);

		MethodSpec methodSpec = buildRelationMethodSpec(methodName, path, responseListType, parameterConfigurer, openApiConfigurer, bodyConfigurer, postBodyConfigurer);
		builder.addMethod(methodSpec);
	}

	/**
	 * Helper method to build the common structure of the {@link MethodSpec} for
	 * the "get multiple relations" method. This includes annotations, modifiers,
	 * the main entity ID path variable, OpenAPI annotations, security injection,
	 * and return type definition.
	 *
	 * @param methodName          The name for the generated method.
	 * @param path                The request mapping path for the method.
	 * @param responseListType    The {@link ParameterizedTypeName} for the list of response entities (List<ResponseEntityType>).
	 * @param parameterConfigurer A {@link Consumer} to add specific parameters (usually none for GET multiple).
	 * @param openApiConfigurer   A {@link Consumer} to add OpenAPI annotations to the method.
	 * @param bodyConfigurer      A {@link Consumer} to add the main method body statements.
	 * @param postBodyConfigurer  A {@link Consumer} to add statements after the main body (for response generation).
	 * @return The constructed {@link MethodSpec}.
	 */
	private MethodSpec buildRelationMethodSpec(String methodName, String path, ParameterizedTypeName responseListType, Consumer<MethodSpec.Builder> parameterConfigurer, Consumer<MethodSpec.Builder> openApiConfigurer, Consumer<MethodSpec.Builder> bodyConfigurer, Consumer<MethodSpec.Builder> postBodyConfigurer) {
		MethodSpec.Builder methodBuilder = MethodSpec
				.methodBuilder(methodName)
				.addAnnotation(AnnotationSpec.builder(GetMapping.class)
						.addMember("value", "$S", path)
						.build())
				.addAnnotation(ResponseBody.class) // Explicitly add for clarity, though often implicit with @RestController
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec.builder(this.idType, "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				);

		// Add specific parameters (usually none needed here)
		parameterConfigurer.accept(methodBuilder);

		// Inject OpenAPI annotations
		openApiConfigurer.accept(methodBuilder);

		// Inject security checks
		methodBuilder = new RelationAuthenticationInjector()
				.withMethod("READ") // Reading relations requires READ permission
				.withEntityClassName(this.entityType)
				.withRelatedClassName(this.relationEntityType)
				.withSecurityConfig(this.securityConfig)
				.inject(methodBuilder);

		// Apply custom injectors from SPI registry.
		if (this.injectorRegistry != null) {
			methodBuilder = this.injectorRegistry.injectAll(methodBuilder, MethodContext.builder()
					.withMethodName(methodName)
					.withHttpMethod("GET")
					.withEntityType(this.entityType)
					.withRelatedEntityType(this.relationEntityType)
					.withRelationName(this.relationName)
					.build());
		}

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

		// Add the main method body logic
		bodyConfigurer.accept(methodBuilder);

		// Add post-body logic (response generation)
		if (postBodyConfigurer != null) {
			postBodyConfigurer.accept(methodBuilder);
		}

		return methodBuilder.build();
	}

	/**
	 * Checks if a request mapping for getting multiple relations
	 * (GET on /base/{id}/relationName) already exists.
	 *
	 * @return {@code true} if a matching mapping exists, {@code false} otherwise.
	 */
	protected boolean hasExistingRequest() {
		return RestnessUtil.hasExistingRequest(
				this.requestMappings,
				this.basePath + "/{id}/" + this.relationName,
				RequestMethod.GET
		);
	}
}