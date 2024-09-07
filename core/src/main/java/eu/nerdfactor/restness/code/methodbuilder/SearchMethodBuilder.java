package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.injector.AuthenticationInjector;
import eu.nerdfactor.restness.code.injector.ReturnStatementInjector;
import eu.nerdfactor.restness.data.DataPage;
import eu.nerdfactor.restness.util.RestnessUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class SearchMethodBuilder extends MethodBuilder {

	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		if (this.configuration.hasExistingRequest(RequestMethod.GET, this.configuration.getRequestBasePath() + "/search")) {
			return builder;
		}
		RestnessUtil.log("addSearchAllEntitiesMethod", 1);
		TypeName responseType = this.configuration.getResponseType();
		ParameterizedTypeName responsePage = ParameterizedTypeName.get(ClassName.get(Page.class), responseType);
		MethodSpec.Builder method = MethodSpec
				.methodBuilder("searchAll")
				.addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "$S", this.configuration.getRequestBasePath() + "/search").build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responsePage))
				.addParameter(ParameterSpec.builder(String.class, "filter")
						.addAnnotation(AnnotationSpec.builder(RequestParam.class)
								.addMember("required", "false").
								build()
						)
						.build()
				)
				.addParameter(ParameterSpec.builder(Pageable.class, "pageable")
						.addAnnotation(AnnotationSpec.builder(PageableDefault.class).addMember("size", "20").build()).
						build()
				);
		method = new AuthenticationInjector()
				.withMethod("READ")
				.withType(this.configuration.getEntityClassName())
				.withSecurityConfig(this.configuration.getSecurityConfiguration())
				.inject(method);
		method.addStatement("$T<$T> spec = this.specificationBuilder.build(filter, $T.class)", Specification.class, this.configuration.getEntityClassName(), this.configuration.getEntityClassName());
		method.addStatement("$T<$T> responseList = new $T<>()", List.class, responseType, ArrayList.class);
		method.addStatement("$T page = this.dataAccessor.searchData(spec, pageable)", ParameterizedTypeName.get(ClassName.get(Page.class), this.configuration.getEntityClassName()));
		method.beginControlFlow("for($T entity : page.getContent())", this.configuration.getEntityClassName());
		if (this.configuration.isUsingDto()) {
			method.addStatement("$T response = this.dataMapper.map(entity, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = entity", responseType);
		}
		method.addStatement("responseList.add(response)");
		method.endControlFlow();
		method.addStatement("$T<$T> responsePage = new $T<>(responseList, page.getPageable(), page.getTotalElements())", Page.class, responseType, DataPage.class);
		method = new ReturnStatementInjector()
				.withWrapper(this.configuration.getResponseWrapperClassName())
				.withResponse(responseType)
				.withResponseVariable("responsePage")
				.inject(method);
		builder.addMethod(method.build());
		return builder;
	}
}