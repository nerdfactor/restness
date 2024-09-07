package eu.nerdfactor.restness.code;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.builder.AuthenticationInjector;
import eu.nerdfactor.restness.code.builder.MethodBuilder;
import eu.nerdfactor.restness.code.builder.ReturnStatementInjector;
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
	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		if (this.configuration.hasExistingRequest(RequestMethod.GET, this.configuration.getRequest() + "/{id}/" + this.relationConfiguration.getName())) {
			return builder;
		}
		RestnessUtil.log("addGetSingleRelationMethod", 1);
		TypeName responseType = this.relationConfiguration.isWithDtos() && this.relationConfiguration.getDtoClass() != null && !this.relationConfiguration.getDtoClass().equals(TypeName.OBJECT) ? this.relationConfiguration.getDtoClass() : this.relationConfiguration.getEntityClass();
		MethodSpec.Builder method = MethodSpec
				.methodBuilder(this.relationConfiguration.getMethodName(AccessorType.GET))
				.addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "$S", this.configuration.getRequest() + "/{id}/" + this.relationConfiguration.getName()).build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responseType))
				.addParameter(ParameterSpec.builder(this.configuration.getId(), "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				);
		method = new AuthenticationInjector()
				.withMethod("READ")
				.withType(this.configuration.getEntity())
				.withRelation(this.relationConfiguration.getEntityClass())
				.withSecurityConfig(this.configuration.getSecurity())
				.inject(method);
		method.addStatement("$T entity = this.dataAccessor.readData(id)", this.configuration.getEntity());
		method.beginControlFlow("if(entity == null)");
		method.addStatement("throw new $T()", EntityNotFoundException.class);
		method.endControlFlow();
		if (this.relationConfiguration.isWithDtos()) {
			method.addStatement("$T response = this.dataMapper.map(entity." + this.relationConfiguration.getGetter() + "(), $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = entity." + this.relationConfiguration.getGetter() + "()", responseType);
		}
		method = new ReturnStatementInjector()
				.withWrapper(this.configuration.getDataWrapperClass())
				.withResponse(responseType)
				.withResponseVariable("response")
				.inject(method);
		builder.addMethod(method.build());
		return builder;
	}
}
