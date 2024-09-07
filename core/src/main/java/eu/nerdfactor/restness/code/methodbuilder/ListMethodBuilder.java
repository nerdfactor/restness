package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.injector.AuthenticationInjector;
import eu.nerdfactor.restness.code.injector.ReturnStatementInjector;
import eu.nerdfactor.restness.util.RestnessUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * A builder that can be used to create a list method in a controller.
 * <p>
 * List method consist of:
 * <li>A method to list all entities.</li>
 *
 * @author Daniel Klug
 */
public class ListMethodBuilder extends MethodBuilder {

	/**
	 * Create a new {@link ListMethodBuilder}.
	 *
	 * @return A new {@link ListMethodBuilder}.
	 */
	public static ListMethodBuilder create() {
		return new ListMethodBuilder();
	}

	/**
	 * Create a {@link TypeSpec.Builder} containing a list method.
	 *
	 * @param builder An existing builder object that will be used.
	 * @return The build {@link TypeSpec.Builder}.
	 */
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		if (this.configuration.hasExistingRequest(RequestMethod.GET, this.configuration.getRequestBasePath())) {
			return builder;
		}
		RestnessUtil.log("addGetAllEntitiesMethod", 1);
		TypeName responseType = this.configuration.getResponseType();
		ParameterizedTypeName responseList = ParameterizedTypeName.get(ClassName.get(List.class), responseType);
		MethodSpec.Builder method = MethodSpec
				.methodBuilder("all")
				.addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "$S", this.configuration.getRequestBasePath()).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseList));
		method = new AuthenticationInjector()
				.withMethod("READ")
				.withEntityClassName(this.configuration.getEntityClassName())
				.withSecurityConfig(this.configuration.getSecurityConfiguration())
				.inject(method);
		method.addStatement("$T<$T> responseList = new $T<>()", List.class, responseType, ArrayList.class);
		method.beginControlFlow("for($T entity : this.dataAccessor.listData())", this.configuration.getEntityClassName());
		if (this.configuration.isUsingDto()) {
			method.addStatement("$T response = this.dataMapper.map(entity, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = entity", responseType);
		}
		method.addStatement("responseList.add(response)");
		method.endControlFlow();
		method = new ReturnStatementInjector()
				.withWrapper(this.configuration.getResponseWrapperClassName())
				.withResponse(responseType)
				.withResponseVariable("responseList")
				.inject(method);
		builder.addMethod(method.build());
		return builder;
	}
}
