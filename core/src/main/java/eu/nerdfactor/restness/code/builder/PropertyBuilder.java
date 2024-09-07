package eu.nerdfactor.restness.code.builder;

import com.squareup.javapoet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;

import javax.lang.model.element.Modifier;

public class PropertyBuilder implements Buildable<TypeSpec.Builder> {

	protected String name;
	protected TypeName type;
	protected boolean hasGetter;
	protected boolean hasSetter;

	public PropertyBuilder withName(String name) {
		this.name = name;
		return this;
	}

	public PropertyBuilder withType(TypeName type) {
		this.type = type;
		return this;
	}

	public PropertyBuilder withType(Class<?> type) {
		this.type = ClassName.get(type);
		return this;
	}

	public PropertyBuilder withProperty(Pair<String, TypeName> prop) {
		this.name = prop.getFirst();
		this.type = prop.getSecond();
		return this;
	}

	public PropertyBuilder withGetter() {
		this.hasGetter = true;
		return this;
	}

	public PropertyBuilder withSetter() {
		this.hasSetter = true;
		return this;
	}

	public String getName() {
		return this.name;
	}

	public TypeName getType() {
		return this.type;
	}

	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		if (this.name == null || this.name.isBlank() || this.type == null) {
			return builder;
		}
		builder.addField(FieldSpec
				.builder(this.type, this.name, Modifier.PROTECTED)
				.build());

		String ucName = this.name.substring(0, 1).toUpperCase() + this.name.substring(1);

		if (this.hasGetter) {
			builder.addMethod(MethodSpec
					.methodBuilder("get" + ucName)
					.addModifiers(Modifier.PUBLIC)
					.returns(this.type)
					.addStatement("return this." + this.name)
					.build());
		}

		if (this.hasSetter) {
			builder.addMethod(MethodSpec
					.methodBuilder("set" + ucName)
					.addAnnotation(Autowired.class)
					.addModifiers(Modifier.PUBLIC)
					.returns(void.class)
					.addParameter(this.type, this.name)
					.addStatement("this." + this.name + " = " + this.name)
					.build());
		}
		return builder;
	}
}
