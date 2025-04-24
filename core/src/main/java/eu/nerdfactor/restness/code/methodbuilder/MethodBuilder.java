package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.builder.Buildable;
import eu.nerdfactor.restness.code.builder.Configurable;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for builders that construct a single method within a controller class.
 * This class implements {@link Configurable} and {@link Buildable} to provide a common
 * structure for method builders that operate based on a {@link ControllerConfiguration}.
 * Subclasses will typically override the {@link #buildWith(TypeSpec.Builder)} method to add
 * a specific method (like a single CRUD operation or a custom endpoint) to the controller
 * being built.
 *
 * @author Daniel Klug
 */
public abstract class MethodBuilder implements Buildable<TypeSpec.Builder>, Configurable<ControllerConfiguration> {

	/**
	 * The {@link ControllerConfiguration} containing settings and type information
	 * used by the builder to generate the controller method.
	 */
	protected ControllerConfiguration configuration;

	/**
	 * Configures the builder with the provided {@link ControllerConfiguration}.
	 * This configuration provides the necessary context (entity types, DTO usage, security, etc.)
	 * for generating the controller method.
	 *
	 * @param configuration The {@link ControllerConfiguration} to use.
	 * @return The current builder instance for fluent chaining.
	 */
	@Override
	public MethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		this.configuration = configuration;
		return this;
	}

	/**
	 * Builds the method defined by the specific builder implementation and adds it
	 * to the provided {@link TypeSpec.Builder}.
	 * This base implementation simply returns the builder unmodified. Subclasses should
	 * override this method to add their generated method.
	 *
	 * @param builder An existing {@link TypeSpec.Builder} representing the controller class
	 *                to which the generated method should be added.
	 * @return The {@link TypeSpec.Builder} potentially modified with the newly added method.
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		return builder;
	}
}
