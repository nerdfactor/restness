package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.injector.AuthenticationInjector;
import eu.nerdfactor.restness.code.builder.Buildable;
import eu.nerdfactor.restness.code.builder.Configurable;
import eu.nerdfactor.restness.code.injector.ReturnStatementInjector;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.config.SecurityConfiguration;
import eu.nerdfactor.restness.util.RestnessUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Modifier;

@With
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadEntityMethodBuilder implements Buildable<TypeSpec.Builder>, Configurable<ControllerConfiguration> {

	protected boolean hasExistingRequest;
	protected String requestUrl;
	protected TypeName responseType;
	protected TypeName entityType;
	protected TypeName identifyingType;
	protected boolean isUsingDto;
	protected SecurityConfiguration securityConfiguration;
	protected TypeName dataWrapperClass;

	public static ReadEntityMethodBuilder create() {
		return new ReadEntityMethodBuilder();
	}

	@Override
	public ReadEntityMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		return new ReadEntityMethodBuilder(
				configuration.hasExistingRequest(RequestMethod.GET, configuration.getRequestBasePath() + "/{id}"),
				configuration.getRequestBasePath() + "/{id}",
				configuration.getSingleResponseType(),
				configuration.getEntityClassName(),
				configuration.getIdClassName(),
				configuration.isUsingDto(),
				configuration.getSecurityConfiguration(),
				configuration.getResponseWrapperClassName()
		);
	}

	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		// Check, if the controller already contains a Get method with the Request Url and an id parameter.
		if (this.hasExistingRequest) {
			return builder;
		}
		RestnessUtil.log("addGetEntityMethod", 1);

		MethodSpec.Builder method = this.createMethodDeclaration(this.requestUrl, this.identifyingType, this.responseType);

		new AuthenticationInjector()
				.withMethod("READ")
				.withEntityClassName(this.entityType)
				.withSecurityConfig(this.securityConfiguration)
				.inject(method);

		this.addMethodBody(method, this.entityType, this.responseType, this.isUsingDto);

		new ReturnStatementInjector()
				.withWrapper(this.dataWrapperClass)
				.withResponse(this.responseType)
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
	 * found.
	 *
	 * @param method       The existing {@link MethodSpec.Builder}.
	 * @param entityType   The type of the Entity.
	 * @param responseType The type of object of the response.
	 * @param isUsingDto   If the method is using DTOs.
	 */
	protected void addMethodBody(MethodSpec.Builder method, TypeName entityType, TypeName responseType, boolean isUsingDto) {
		method.addStatement("$T entity = this.dataAccessor.readData(id)", entityType);
		method.beginControlFlow("if(entity == null)");
		method.addStatement("throw new $T()", EntityNotFoundException.class);
		method.endControlFlow();
		if (isUsingDto) {
			method.addStatement("$T response = this.dataMapper.map(entity, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = entity", responseType);
		}
	}


}
