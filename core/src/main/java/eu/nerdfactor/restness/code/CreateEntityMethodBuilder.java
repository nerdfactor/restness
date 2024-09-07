package eu.nerdfactor.restness.code;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.builder.AuthenticationInjector;
import eu.nerdfactor.restness.code.builder.Buildable;
import eu.nerdfactor.restness.code.builder.Configurable;
import eu.nerdfactor.restness.code.builder.ReturnStatementInjector;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.config.SecurityConfiguration;
import eu.nerdfactor.restness.util.RestnessUtil;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Modifier;

@With
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateEntityMethodBuilder implements Buildable<TypeSpec.Builder>, Configurable<ControllerConfiguration> {

	protected boolean hasExistingRequest;
	protected String requestUrl;
	protected TypeName requestType;
	protected TypeName responseType;
	protected TypeName entityType;
	protected boolean isUsingDto;
	protected SecurityConfiguration securityConfiguration;
	protected TypeName dataWrapperClass;

	public static CreateEntityMethodBuilder create() {
		return new CreateEntityMethodBuilder();
	}

	@Override
	public CreateEntityMethodBuilder withConfiguration(ControllerConfiguration configuration) {
		return new CreateEntityMethodBuilder(
				configuration.hasExistingRequest(RequestMethod.POST, configuration.getRequestBasePath()),
				configuration.getRequestBasePath(),
				configuration.getRequestType(),
				configuration.getSingleResponseType(),
				configuration.getEntityClassName(),
				configuration.isUsingDto(),
				configuration.getSecurityConfiguration(),
				configuration.getResponseWrapperClassName()
		);
	}

	@Override
	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		// Check, if the controller already contains a Post method with the Request Url.
		if (this.hasExistingRequest) {
			return builder;
		}
		RestnessUtil.log("addCreateEntityMethod", 1);

		// Create the method declaration.
		MethodSpec.Builder method = this.createMethodDeclaration(this.requestUrl, this.requestType, this.responseType);

		// Inject a Security Annotation that will require a role of "CREATE"
		// for the Entity.
		new AuthenticationInjector().withMethod("CREATE")
				.withType(this.entityType)
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
	 * with the help of the DataAccessor and return the result.
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
