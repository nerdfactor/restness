package eu.nerdfactor.restness.code.builder;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ConstructorBuilder implements Buildable<TypeSpec.Builder> {

	List<Pair<String, TypeName>> properties = new ArrayList<>();

	public ConstructorBuilder withProperty(String name, TypeName type) {
		this.properties.add(Pair.of(name, type));
		return this;
	}

	public ConstructorBuilder withProperty(Pair<String, TypeName> prop) {
		this.properties.add(prop);
		return this;
	}

	public ConstructorBuilder withProperty(PropertyBuilder propertyBuilder) {
		return this.withProperty(propertyBuilder.getName(), propertyBuilder.getType());
	}

	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		final MethodSpec.Builder method = MethodSpec
				.constructorBuilder()
				.addAnnotation(Autowired.class)
				.addModifiers(Modifier.PUBLIC);
		this.properties.forEach(prop -> {
			method.addParameter(prop.getSecond(), prop.getFirst());
			method.addStatement("this." + prop.getFirst() + " = " + prop.getFirst());
		});
		builder.addMethod(method.build());
		return builder;
	}
}
