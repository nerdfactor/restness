package eu.nerdfactor.restness.code.builder;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * A builder that creates a constructor with a list of parameters.
 *
 * @author Daniel Klug
 */
public class ConstructorBuilder implements Buildable<TypeSpec.Builder> {

	/**
	 * A list of properties that are set by the constructor.
	 */
	protected final List<PropertyPair> properties = new ArrayList<>();

	/**
	 * Add a property to the constructor.
	 *
	 * @param name The name of the property.
	 * @param type The {@link TypeName} of the property.
	 * @return The builder in a fluent api pattern.
	 */
	public ConstructorBuilder withProperty(String name, TypeName type) {
		this.properties.add(new PropertyPair(name, type));
		return this;
	}

	/**
	 * Add a property to the constructor.
	 *
	 * @param prop The {@link PropertyPair} containing name and {@link TypeName} of the property.
	 * @return The builder in a fluent api pattern.
	 */
	public ConstructorBuilder withProperty(PropertyPair prop) {
		this.properties.add(prop);
		return this;
	}

	/**
	 * Add a property to the constructor.
	 *
	 * @param propertyBuilder A {@link PropertyBuilder} that provides the name and {@link TypeName} of the property.
	 * @return The builder in a fluent api pattern.
	 */
	public ConstructorBuilder withProperty(PropertyBuilder propertyBuilder) {
		return this.withProperty(propertyBuilder.getName(), propertyBuilder.getType());
	}

	/**
	 * Create a {@link TypeSpec.Builder} containing the constructor.
	 *
	 * @param builder An existing builder object that will be appended.
	 * @return The build {@link TypeSpec.Builder}.
	 */
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		final MethodSpec.Builder method = MethodSpec
				.constructorBuilder()
				.addAnnotation(Autowired.class)
				.addModifiers(Modifier.PUBLIC);
		this.properties.forEach(prop -> {
			method.addParameter(prop.type(), prop.name());
			method.addStatement("this." + prop.name() + " = " + prop.name());
		});
		builder.addMethod(method.build());
		return builder;
	}
}
