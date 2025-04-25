package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.builder.Buildable;
import eu.nerdfactor.restness.code.builder.Configurable;
import eu.nerdfactor.restness.code.injector.AuthenticationInjector;
import eu.nerdfactor.restness.code.injector.NoContentStatementInjector;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Modifier;

/**
 * Builder for creating the method that deletes an existing entity in a REST controller.
 * This builder is responsible for generating the {@code delete(id)} method, typically
 * annotated with {@code @DeleteMapping("/{id}")}. It handles checking for existing methods,
 * injecting authentication logic, defining the method body for deleting the entity,
 * and returning a no-content response.
 */
@Slf4j
@With
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class DeleteEntityMethodBuilder implements Buildable<TypeSpec.Builder>, Configurable<ControllerConfiguration> {

	/**
	 * Flag indicating if a request mapping for deleting an entity already exists.
	 */
	protected boolean hasExistingRequest;
	/**
	 * The URL path for the delete entity request (e.g., "/entities/{id}").
	 */
	protected String requestUrl;
	/**
	 * The {@link TypeName} of the entity being deleted.
	 */
	protected TypeName entityType;
	/**
	 * The {@link TypeName} of the entity's identifier (e.g., Long, String).
	 */
	protected TypeName identifyingType;
	/**
	 * Security configuration for the controller method.
	 */
	protected SecurityConfiguration securityConfiguration;
	/**
	 * The {@link TypeName} of the class used to wrap the response data, if any.
	 * Although delete typically returns no content, a wrapper might be used for consistency.
	 */
	protected TypeName dataWrapperClass;

	/**
	 * Creates a new instance of {@link DeleteEntityMethodBuilder}.
	 *
	 * @return A new {@link DeleteEntityMethodBuilder}.
	 */
	public static DeleteEntityMethodBuilder create() {
		return new DeleteEntityMethodBuilder();
	}

	/**
	 * Configures the builder with the provided {@link ControllerConfiguration}.
	 * It extracts necessary information like request paths, types, security settings,
	 * and response wrapping.
	 *
	 * @param configuration The {@link ControllerConfiguration} for the controller.
	 * @return A new configured instance of {@link DeleteEntityMethodBuilder}.
	 */
	@Override
	public DeleteEntityMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		return this.withHasExistingRequest(RestnessUtil.hasExistingRequest(configuration.getExistingRequestMappings(), configuration.getRequestBasePath() + "/{id}", RequestMethod.DELETE))
				.withRequestUrl(configuration.getRequestBasePath() + "/{id}")
				.withEntityType(configuration.getEntityClassName())
				.withIdentifyingType(configuration.getIdClassName())
				.withSecurityConfiguration(configuration.getSecurityConfiguration())
				.withDataWrapperClass(configuration.getResponseWrapperClassName());
	}

	/**
	 * Builds the delete entity method and adds it to the provided {@link TypeSpec.Builder}.
	 * If a method with the same signature already exists, it skips the generation.
	 * Otherwise, it creates the method declaration, injects authentication, adds the
	 * method body (calling the data accessor to delete), and injects the no-content
	 * return statement logic.
	 *
	 * @param builder The {@link TypeSpec.Builder} for the controller class.
	 * @return The updated {@link TypeSpec.Builder} with the new method added (if applicable).
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		if (this.hasExistingRequest) {
			return builder;
		}
		log.info("addDeleteEntityMethod");

		MethodSpec.Builder method = this.createMethodDeclaration(this.requestUrl, this.identifyingType);

		new AuthenticationInjector()
				.withMethod("DELETE")
				.withEntityClassName(this.entityType)
				.withSecurityConfig(this.securityConfiguration)
				.inject(method);

		this.addMethodBody(method);

		new NoContentStatementInjector()
				.withWrapper(this.dataWrapperClass)
				.inject(method);

		builder.addMethod(method.build());
		return builder;
	}

	/**
	 * Create a Delete method called "delete" with the requestUrl that takes
	 * an identifyingType (called "id") from the PathVariable and will return
	 * a ResponseEntity.
	 *
	 * @param requestUrl      The requested Url.
	 * @param identifyingType The type of object identifying the Entity.
	 * @return The {@link MethodSpec.Builder} of the new method declaration.
	 */
	protected MethodSpec.Builder createMethodDeclaration(String requestUrl, TypeName identifyingType) {
		return MethodSpec.methodBuilder("delete")
				.addAnnotation(AnnotationSpec.builder(DeleteMapping.class).addMember("value", "$S", requestUrl).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), WildcardTypeName.subtypeOf(Object.class)))
				.addParameter(ParameterSpec.builder(identifyingType, "id").addModifiers(Modifier.FINAL).addAnnotation(PathVariable.class).build());
	}

	/**
	 * Add a method body that deletes the Entity identified by the provided id
	 * using the DataAccessor.
	 *
	 * @param method The existing {@link MethodSpec.Builder}.
	 */
	protected void addMethodBody(MethodSpec.Builder method) {
		method.addStatement("this.dataAccessor.deleteDataById(id)");
	}
}
