package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.injector.AuthenticationInjector;
import eu.nerdfactor.restness.code.injector.ReturnStatementInjector;
import eu.nerdfactor.restness.config.AccessorType;
import eu.nerdfactor.restness.config.RelationConfiguration;
import eu.nerdfactor.restness.util.RestnessUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Modifier;

public class GetSingleRelationMethodBuilder extends MethodBuilder {

	RelationConfiguration relationConfiguration;

	public GetSingleRelationMethodBuilder withRelation(RelationConfiguration relation) {
		this.relationConfiguration = relation;
		return this;
	}

	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		if (this.configuration.hasExistingRequest(RequestMethod.GET, this.configuration.getRequestBasePath() + "/{id}/" + this.relationConfiguration.getRelationName())) {
			return builder;
		}
		RestnessUtil.log("addGetSingleRelationMethod", 1);
		TypeName responseType = this.relationConfiguration.isUsingDto() && this.relationConfiguration.getResponseObjectClassName() != null && !this.relationConfiguration.getResponseObjectClassName().equals(TypeName.OBJECT) ? this.relationConfiguration.getResponseObjectClassName() : this.relationConfiguration.getEntityClassName();
		MethodSpec.Builder method = MethodSpec
				.methodBuilder(this.relationConfiguration.getMethodName(AccessorType.GET))
				.addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "$S", this.configuration.getRequestBasePath() + "/{id}/" + this.relationConfiguration.getRelationName()).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseType))
				.addParameter(ParameterSpec.builder(this.configuration.getIdClassName(), "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				);
		method = new AuthenticationInjector()
				.withMethod("READ")
				.withEntityClassName(this.configuration.getEntityClassName())
				.withRelatedClassName(this.relationConfiguration.getEntityClassName())
				.withSecurityConfig(this.configuration.getSecurityConfiguration())
				.inject(method);
		method.addStatement("$T entity = this.dataAccessor.readData(id).orElseThrow($T::new)", this.configuration.getEntityClassName(), EntityNotFoundException.class);
		if (this.relationConfiguration.isUsingDto()) {
			method.addStatement("$T response = this.dataMapper.map(entity." + this.relationConfiguration.getGetterMethodName() + "(), $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = entity." + this.relationConfiguration.getGetterMethodName() + "()", responseType);
		}
		method = new ReturnStatementInjector()
				.withWrapper(this.configuration.getResponseWrapperClassName())
				.withResponse(responseType)
				.withResponseVariable("response")
				.inject(method);
		builder.addMethod(method.build());
		return builder;
	}
}
