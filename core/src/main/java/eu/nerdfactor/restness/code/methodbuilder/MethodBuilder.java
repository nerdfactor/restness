package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.builder.Buildable;
import eu.nerdfactor.restness.code.builder.Configurable;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * A builder that can be used to create a method in a controller.
 *
 * @author Daniel Klug
 */
public abstract class MethodBuilder implements Buildable<TypeSpec.Builder>, Configurable<ControllerConfiguration> {

	/**
	 * The {@link ControllerConfiguration} used to create the method.
	 */
	protected ControllerConfiguration configuration;

	/**
	 * Set the {@link ControllerConfiguration} that will be used.
	 *
	 * @param configuration The {@link ControllerConfiguration}.
	 * @return The builder in a fluent api pattern.
	 */
	@Override
	public MethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		this.configuration = configuration;
		return this;
	}

	/**
	 * Create a {@link TypeSpec.Builder} containing a method.
	 *
	 * @param builder An existing builder object that will be used.
	 * @return The build {@link TypeSpec.Builder}.
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		return builder;
	}
}
