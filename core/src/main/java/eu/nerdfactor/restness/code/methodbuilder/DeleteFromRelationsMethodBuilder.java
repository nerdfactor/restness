package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.injector.AuthenticationInjector;
import eu.nerdfactor.restness.code.injector.NoContentStatementInjector;
import eu.nerdfactor.restness.config.AccessorType;
import eu.nerdfactor.restness.config.RelationConfiguration;
import eu.nerdfactor.restness.util.RestnessUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.Modifier;
import java.util.List;

public class DeleteFromRelationsMethodBuilder extends MethodBuilder {

	RelationConfiguration relationConfiguration;

	public DeleteFromRelationsMethodBuilder withRelation(RelationConfiguration relation) {
		this.relationConfiguration = relation;
		return this;
	}

	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		if (this.configuration.hasExistingRequest(RequestMethod.DELETE, this.configuration.getRequestBasePath() + "/{id}/" + this.relationConfiguration.getRelationName())) {
			return builder;
		}
		RestnessUtil.log("addDeleteFromRelationsMethod", 1);
		TypeName responseType = this.relationConfiguration.isUsingDto() && this.relationConfiguration.getResponseObjectClassName() != null && !this.relationConfiguration.getResponseObjectClassName().equals(TypeName.OBJECT) ? this.relationConfiguration.getResponseObjectClassName() : this.relationConfiguration.getEntityClassName();
		ParameterizedTypeName responseList = ParameterizedTypeName.get(ClassName.get(List.class), responseType);
		MethodSpec.Builder method = MethodSpec
				.methodBuilder(this.relationConfiguration.getMethodName(AccessorType.REMOVE))
				.addAnnotation(AnnotationSpec.builder(DeleteMapping.class).addMember("value", "$S", this.configuration.getRequestBasePath() + "/{id}/" + this.relationConfiguration.getRelationName()).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseList))
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
				.withEntityClassName(this.configuration.getEntityClassName())
				.withRelatedClassName(this.relationConfiguration.getEntityClassName())
				.withSecurityConfig(this.configuration.getSecurityConfiguration())
				.inject(method);
		if (this.configuration.getResponseWrapperClassName() != null && !this.configuration.getResponseWrapperClassName().equals(TypeName.OBJECT)) {
			method.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), ParameterizedTypeName.get(ClassName.bestGuess(this.configuration.getResponseWrapperClassName().toString()), responseType)));
		}
		method.addStatement("return this." + this.relationConfiguration.getMethodName(AccessorType.REMOVE) + "ById(id, dto." + this.relationConfiguration.getIdAccessorMethodName() + "())");
		builder.addMethod(method.build());

		if (this.configuration.hasExistingRequest(RequestMethod.DELETE, this.configuration.getRequestBasePath() + "/{id}/" + this.relationConfiguration.getRelationName() + "/{relationId}")) {
			return builder;
		}
		MethodSpec.Builder methodById = MethodSpec
				.methodBuilder(this.relationConfiguration.getMethodName(AccessorType.REMOVE) + "ById")
				.addAnnotation(AnnotationSpec.builder(DeleteMapping.class).addMember("value", "$S", this.configuration.getRequestBasePath() + "/{id}/" + this.relationConfiguration.getRelationName() + "/{relationId}").build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseList))
				.addParameter(ParameterSpec.builder(this.configuration.getIdClassName(), "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				)
				.addParameter(ParameterSpec.builder(this.relationConfiguration.getIdClassName(), "relationId")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				);
		methodById = new AuthenticationInjector()
				.withMethod("UPDATE")
				.withEntityClassName(this.configuration.getEntityClassName())
				.withRelatedClassName(this.relationConfiguration.getEntityClassName())
				.withSecurityConfig(this.configuration.getSecurityConfiguration())
				.inject(methodById);
		methodById.addStatement("$T entity = this.dataAccessor.readData(id).orElseThrow($T::new)", this.configuration.getEntityClassName(), EntityNotFoundException.class);
		methodById.addStatement("$T rel = this.entityManager.getReference($T.class, relationId)", this.relationConfiguration.getEntityClassName(), this.relationConfiguration.getEntityClassName());
		methodById.addStatement("entity." + this.relationConfiguration.getRemoverMethodName() + "(rel)");
		methodById.addStatement("this.dataAccessor.updateData(entity)");
		methodById = new NoContentStatementInjector()
				.withWrapper(this.configuration.getResponseWrapperClassName())
				.withResponse(responseType)
				.inject(methodById);
		builder.addMethod(methodById.build());
		return builder;
	}
}
