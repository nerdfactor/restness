package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.injector.AuthenticationInjector;
import eu.nerdfactor.restness.config.AccessorType;
import eu.nerdfactor.restness.config.RelationConfiguration;
import eu.nerdfactor.restness.util.RestnessUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.Modifier;

public class SetSingleRelationMethodBuilder extends MethodBuilder {

	RelationConfiguration relationConfiguration;

	public SetSingleRelationMethodBuilder withRelation(RelationConfiguration relation) {
		this.relationConfiguration = relation;
		return this;
	}

	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		if (this.configuration.hasExistingRequest(RequestMethod.POST, this.configuration.getRequestBasePath() + "/{id}/" + this.relationConfiguration.getRelationName()) ||
				this.configuration.hasExistingRequest(RequestMethod.PUT, this.configuration.getRequestBasePath() + "/{id}/" + this.relationConfiguration.getRelationName()) ||
				this.configuration.hasExistingRequest(RequestMethod.PATCH, this.configuration.getRequestBasePath() + "/{id}/" + this.relationConfiguration.getRelationName())) {
			return builder;
		}
		RestnessUtil.log("addSetSingleRelationMethod", 1);
		TypeName responseType = this.relationConfiguration.isUsingDto() && this.relationConfiguration.getResponseObjectClassName() != null && !this.relationConfiguration.getResponseObjectClassName().equals(TypeName.OBJECT) ? this.relationConfiguration.getResponseObjectClassName() : this.relationConfiguration.getEntityClassName();
		MethodSpec.Builder method = MethodSpec
				.methodBuilder(this.relationConfiguration.getMethodName(AccessorType.SET))
				.addAnnotation(AnnotationSpec.builder(RequestMapping.class).addMember("value", "$S", this.configuration.getRequestBasePath() + "/{id}/" + this.relationConfiguration.getRelationName()).addMember("method", "{ $T.POST, $T.PUT, $T.PATCH }", RequestMethod.class, RequestMethod.class, RequestMethod.class).build())
				.addAnnotation(ResponseBody.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseType))
				.addParameter(ParameterSpec.builder(this.configuration.getIdClassName(), "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				)
				.addParameter(ParameterSpec.builder(responseType, "dto")
						.addAnnotation(RequestBody.class)
						.addAnnotation(Valid.class)
						.build()
				);
		method = new AuthenticationInjector()
				.withMethod("UPDATE")
				.withType(this.configuration.getEntityClassName())
				.withRelation(this.relationConfiguration.getEntityClassName())
				.withSecurityConfig(this.configuration.getSecurityConfiguration())
				.inject(method);
		method.addStatement("$T entity = this.dataAccessor.readData(id)", this.configuration.getEntityClassName());
		method.beginControlFlow("if(entity == null)");
		method.addStatement("throw new $T()", EntityNotFoundException.class);
		method.endControlFlow();
		if (this.relationConfiguration.isUsingDto()) {
			method.addStatement("$T rel = this.dataMapper.map(dto, $T.class)", this.relationConfiguration.getEntityClassName(), this.relationConfiguration.getEntityClassName());
		} else {
			method.addStatement("$T rel = dto", this.relationConfiguration.getEntityClassName());
		}
		method.addStatement("entity." + this.relationConfiguration.getSetterMethodName() + "(rel)");
		if (this.configuration.getResponseWrapperClassName() != null && !this.configuration.getResponseWrapperClassName().equals(TypeName.OBJECT)) {
			method.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), ParameterizedTypeName.get(ClassName.bestGuess(this.configuration.getResponseWrapperClassName().toString()), responseType)));
		}
		method.addStatement("return this." + this.relationConfiguration.getMethodName(AccessorType.GET) + "(id)");
		builder.addMethod(method.build());
		return builder;
	}
}
