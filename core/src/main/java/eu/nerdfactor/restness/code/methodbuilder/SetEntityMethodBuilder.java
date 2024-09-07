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
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Modifier;

@With
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SetEntityMethodBuilder implements Buildable<TypeSpec.Builder>, Configurable<ControllerConfiguration> {

	protected boolean hasExistingRequest;
	protected String requestUrl;
	protected TypeName requestType;
	protected TypeName responseType;
	protected TypeName entityType;
	protected TypeName identifyingType;
	protected boolean isUsingDto;
	protected SecurityConfiguration securityConfiguration;
	protected TypeName dataWrapperClass;

	public static SetEntityMethodBuilder create() {
		return new SetEntityMethodBuilder();
	}

	@Override
	public SetEntityMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		return new SetEntityMethodBuilder(
				configuration.hasExistingRequest(RequestMethod.PUT, configuration.getRequestBasePath() + "/{id}"),
				configuration.getRequestBasePath() + "/{id}",
				configuration.getRequestType(),
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
		if (this.hasExistingRequest) {
			return builder;
		}
		RestnessUtil.log("addSetEntityMethod", 1);

		MethodSpec.Builder method = this.createMethodDeclaration(this.requestUrl, this.identifyingType, this.responseType, this.requestType);

		new AuthenticationInjector()
				.withMethod("UPDATE")
				.withType(this.entityType)
				.withSecurityConfig(this.securityConfiguration)
				.inject(method);

		this.addMethodBody(method, this.entityType, this.responseType, this.isUsingDto);

		method = new ReturnStatementInjector()
				.withWrapper(this.dataWrapperClass)
				.withResponseVariable("response")
				.inject(method);
		builder.addMethod(method.build());
		return builder;
	}

	/**
	 * Create a Put method called "set" with the requestUrl that takes a Valid
	 * object of requestType from the RequestBody (called "dto") and will return an
	 * ResponseEntity with an object of responseType.
	 *
	 * @param requestUrl      The requested Url.
	 * @param identifyingType The type of object identifying the Entity.
	 * @param responseType    The type of object of the response.
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
	 * DataAccessor and the provided id and saves the Entity as new
	 * object with the help of the DataAccessor and return the result.
	 * Will throw a new EntityNotFoundException if no Entity could be
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
			method.addStatement("$T changed = this.dataMapper.map(dto, $T.class)", entityType, entityType);
		} else {
			method.addStatement("$T changed = dto", entityType);
		}
		method.addStatement("changed = this.dataAccessor.updateData(changed)");
		if (isUsingDto) {
			method.addStatement("$T response = this.dataMapper.map(changed, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = changed", responseType);
		}
	}
}
