package eu.nerdfactor.restness.code;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.builder.AuthenticationInjector;
import eu.nerdfactor.restness.code.builder.MethodBuilder;
import eu.nerdfactor.restness.code.builder.ReturnStatementInjector;
import eu.nerdfactor.restness.util.GeneratedRestUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ListMethodBuilder extends MethodBuilder {

	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		if (this.configuration.hasExistingRequest(RequestMethod.GET, this.configuration.getRequest())) {
			return builder;
		}
		GeneratedRestUtil.log("addGetAllEntitiesMethod", 1);
		TypeName responseType = this.configuration.getResponseType();
		ParameterizedTypeName responseList = ParameterizedTypeName.get(ClassName.get(List.class), responseType);
		MethodSpec.Builder method = MethodSpec
				.methodBuilder("all")
				.addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "$S", this.configuration.getRequest()).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseList));
		method = new AuthenticationInjector()
				.withMethod("READ")
				.withType(this.configuration.getEntity())
				.withSecurityConfig(this.configuration.getSecurity())
				.inject(method);
		method.addStatement("$T<$T> responseList = new $T<>()", List.class, responseType, ArrayList.class);
		method.beginControlFlow("for($T entity : this.dataAccessor.listData())", this.configuration.getEntity());
		if (this.configuration.isUsingDto()) {
			method.addStatement("$T response = this.dataMapper.map(entity, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = entity", responseType);
		}
		method.addStatement("responseList.add(response)");
		method.endControlFlow();
		method = new ReturnStatementInjector()
				.withWrapper(this.configuration.getDataWrapperClass())
				.withResponse(responseType)
				.withResponseVariable("responseList")
				.inject(method);
		builder.addMethod(method.build());
		return builder;
	}
}
