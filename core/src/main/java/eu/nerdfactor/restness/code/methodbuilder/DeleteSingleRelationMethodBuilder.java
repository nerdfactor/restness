package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.injector.AuthenticationInjector;
import eu.nerdfactor.restness.code.injector.NoContentStatementInjector;
import eu.nerdfactor.restness.config.AccessorType;
import eu.nerdfactor.restness.config.RelationConfiguration;
import eu.nerdfactor.restness.util.RestnessUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.lang.model.element.Modifier;

public class DeleteSingleRelationMethodBuilder extends MethodBuilder {

	RelationConfiguration relationConfiguration;

	public DeleteSingleRelationMethodBuilder withRelation(RelationConfiguration relation) {
		this.relationConfiguration = relation;
		return this;
	}

	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		if (this.configuration.hasExistingRequest(RequestMethod.DELETE, this.configuration.getRequestBasePath() + "/{id}/" + this.relationConfiguration.getRelationName())) {
			return builder;
		}
		RestnessUtil.log("addDeleteSingleRelationMethod", 1);
		TypeName responseType = this.relationConfiguration.isUsingDto() && this.relationConfiguration.getResponseObjectClassName() != null && !this.relationConfiguration.getResponseObjectClassName().equals(TypeName.OBJECT) ? this.relationConfiguration.getResponseObjectClassName() : this.relationConfiguration.getEntityClassName();
		MethodSpec.Builder method = MethodSpec
				.methodBuilder(this.relationConfiguration.getMethodName(AccessorType.REMOVE))
				.addAnnotation(AnnotationSpec.builder(DeleteMapping.class).addMember("value", "$S", this.configuration.getRequestBasePath() + "/{id}/" + this.relationConfiguration.getRelationName()).build())
				.addAnnotation(ResponseBody.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(ClassName.get(ResponseEntity.class))
				.addParameter(ParameterSpec.builder(this.configuration.getIdClassName(), "id")
						.addModifiers(Modifier.FINAL)
						.addAnnotation(PathVariable.class)
						.build()
				);
		method = new AuthenticationInjector()
				.withMethod("UPDATE")
				.withEntityClassName(this.configuration.getEntityClassName())
				.withRelatedClassName(this.relationConfiguration.getEntityClassName())
				.withSecurityConfig(this.configuration.getSecurityConfiguration())
				.inject(method);
		method.addStatement("$T entity = this.dataAccessor.readData(id).orElseThrow($T::new)", this.configuration.getEntityClassName(), EntityNotFoundException.class);
		method.addStatement("entity." + this.relationConfiguration.getSetterMethodName() + "(null)");
		method.addStatement("this.dataAccessor.updateData(entity)");
		method = new NoContentStatementInjector()
				.withWrapper(this.configuration.getResponseWrapperClassName())
				.withResponse(responseType)
				.inject(method);
		builder.addMethod(method.build());
		return builder;
	}
}
