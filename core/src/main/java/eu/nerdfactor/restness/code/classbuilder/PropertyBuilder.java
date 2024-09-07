package eu.nerdfactor.restness.code.classbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.builder.Buildable;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.lang.model.element.Modifier;

/**
 * A builder that creates a class property with getter and setter.
 *
 * @author Daniel Klug
 */
public class PropertyBuilder implements Buildable<TypeSpec.Builder> {

	/**
	 * The name of the property.
	 */
	@Getter
	protected String name;

	/**
	 * The {@link TypeName} of the property.
	 */
	@Getter
	protected TypeName type;

	/**
	 * If the property should have a getter.
	 */
	protected boolean hasGetter;

	/**
	 * If the property should have a setter.
	 */
	protected boolean hasSetter;

	/**
	 * Set the name of the property.
	 *
	 * @param name The name of the property.
	 * @return The builder in a fluent api pattern.
	 */
	public PropertyBuilder withName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Set the {@link TypeName} of the property.
	 *
	 * @param type The {@link TypeName} of the property.
	 * @return The builder in a fluent api pattern.
	 */
	public PropertyBuilder withType(TypeName type) {
		this.type = type;
		return this;
	}

	/**
	 * Set the {@link TypeName} of the property.
	 *
	 * @param type A {@link Class} that will be used as the properties type.
	 * @return The builder in a fluent api pattern.
	 */
	public PropertyBuilder withType(Class<?> type) {
		this.type = ClassName.get(type);
		return this;
	}

	/**
	 * Set the name and {@link TypeName} of the property.
	 *
	 * @param name The name of the property.
	 * @param type The {@link TypeName} of the property.
	 * @return The builder in a fluent api pattern.
	 */
	public PropertyBuilder withProperty(String name, TypeName type) {
		this.name = name;
		this.type = type;
		return this;
	}

	/**
	 * Set the property to use a getter.
	 *
	 * @return The builder in a fluent api pattern.
	 */
	public PropertyBuilder withGetter() {
		this.hasGetter = true;
		return this;
	}

	/**
	 * Set the property to use a setter.
	 *
	 * @return The builder in a fluent api pattern.
	 */
	public PropertyBuilder withSetter() {
		this.hasSetter = true;
		return this;
	}

	/**
	 * Create a {@link TypeSpec.Builder} containing the properties with getters and setters.
	 *
	 * @param builder An existing builder object that will be appended.
	 * @return The build {@link TypeSpec.Builder}.
	 */
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
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
