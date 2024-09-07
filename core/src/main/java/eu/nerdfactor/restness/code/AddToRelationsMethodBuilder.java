package eu.nerdfactor.restness.code;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.builder.AuthenticationInjector;
import eu.nerdfactor.restness.code.builder.MethodBuilder;
import eu.nerdfactor.restness.config.AccessorType;
import eu.nerdfactor.restness.config.RelationConfiguration;
import eu.nerdfactor.restness.util.GeneratedRestUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.Modifier;
import java.util.List;

public class AddToRelationsMethodBuilder extends MethodBuilder {

	RelationConfiguration relationConfiguration;

	public AddToRelationsMethodBuilder withRelation(RelationConfiguration relation) {
		this.relationConfiguration = relation;
		return this;
	}

	@Override
	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		if (this.configuration.hasExistingRequest(RequestMethod.POST, this.configuration.getRequest() + "/{id}/" + this.relationConfiguration.getName()) ||
				this.configuration.hasExistingRequest(RequestMethod.PUT, this.configuration.getRequest() + "/{id}/" + this.relationConfiguration.getName()) ||
				this.configuration.hasExistingRequest(RequestMethod.PATCH, this.configuration.getRequest() + "/{id}/" + this.relationConfiguration.getName())) {
			return builder;
		}
		GeneratedRestUtil.log("addAddToRelationsMethod", 1);
		TypeName responseType = this.relationConfiguration.isWithDtos() && this.relationConfiguration.getDtoClass() != null && !this.relationConfiguration.getDtoClass().equals(TypeName.OBJECT) ? this.relationConfiguration.getDtoClass() : this.relationConfiguration.getEntityClass();
		ParameterizedTypeName responseList = ParameterizedTypeName.get(ClassName.get(List.class), responseType);
		MethodSpec.Builder method = MethodSpec
				.methodBuilder(this.relationConfiguration.getMethodName(AccessorType.ADD))
				.addAnnotation(AnnotationSpec.builder(RequestMapping.class).addMember("value", "$S", this.configuration.getRequest() + "/{id}/" + this.relationConfiguration.getName()).addMember("method", "{ $T.POST, $T.PUT, $T.PATCH }", RequestMethod.class, RequestMethod.class, RequestMethod.class).build())
				.addAnnotation(ResponseBody.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseList))
				.addParameter(ParameterSpec.builder(this.configuration.getId(), "id")
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
				.withType(this.configuration.getEntity())
				.withRelation(this.relationConfiguration.getEntityClass())
				.withSecurityConfig(this.configuration.getSecurity())
				.inject(method);
		if (this.configuration.getDataWrapperClass() != null && !this.configuration.getDataWrapperClass().equals(TypeName.OBJECT)) {
			method.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), ParameterizedTypeName.get(ClassName.bestGuess(this.configuration.getDataWrapperClass().toString()), responseType)));
		}
		method.addStatement("return this." + this.relationConfiguration.getMethodName(AccessorType.ADD) + "ById(id, dto." + this.relationConfiguration.getIdAccessor() + "())");
		builder.addMethod(method.build());


		if (this.configuration.hasExistingRequest(RequestMethod.POST, this.configuration.getRequest() + "/{id}/" + this.relationConfiguration.getName() + "/{relationId}") ||
				this.configuration.hasExistingRequest(RequestMethod.PUT, this.configuration.getRequest() + "/{id}/" + this.relationConfiguration.getName() + "/{relationId}") ||
				this.configuration.hasExistingRequest(RequestMethod.PATCH, this.configuration.getRequest() + "/{id}/" + this.relationConfiguration.getName() + "/{relationId}")) {
			return builder;
		}
		MethodSpec.Builder methodById = MethodSpec
				.methodBuilder(this.relationConfiguration.getMethodName(AccessorType.ADD) + "ById")
				.addAnnotation(AnnotationSpec.builder(RequestMapping.class).addMember("value", "$S", this.configuration.getRequest() + "/{id}/" + this.relationConfiguration.getName() + "/{relationId}").addMember("method", "{ $T.POST, $T.PUT, $T.PATCH }", RequestMethod.class, RequestMethod.class, RequestMethod.class).build())
				.addAnnotation(ResponseBody.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseList))
				.addParameter(ParameterSpec.builder(this.configuration.getId(), "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				)
				.addParameter(ParameterSpec.builder(this.relationConfiguration.getIdClass(), "relationId")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				);
		methodById = new AuthenticationInjector()
				.withMethod("UPDATE")
				.withType(this.configuration.getEntity())
				.withRelation(this.relationConfiguration.getEntityClass())
				.withSecurityConfig(this.configuration.getSecurity())
				.inject(methodById);
		methodById.addStatement("$T entity = this.dataAccessor.readData(id)", this.configuration.getEntity());
		methodById.beginControlFlow("if(entity == null)");
		methodById.addStatement("throw new $T()", EntityNotFoundException.class);
		methodById.endControlFlow();
		methodById.addStatement("$T rel = this.entityManager.getReference($T.class, relationId)", this.relationConfiguration.getEntityClass(), this.relationConfiguration.getEntityClass());
		methodById.addStatement("entity." + this.relationConfiguration.getAdder() + "(rel)");
		if (this.configuration.getDataWrapperClass() != null && !this.configuration.getDataWrapperClass().equals(TypeName.OBJECT)) {
			methodById.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), ParameterizedTypeName.get(ClassName.bestGuess(this.configuration.getDataWrapperClass().toString()), responseType)));
		}
		methodById.addStatement("return this." + this.relationConfiguration.getMethodName(AccessorType.GET) + "(id)");
		builder.addMethod(methodById.build());
		return builder;
	}
}
