package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.builder.Buildable;
import eu.nerdfactor.restness.code.builder.Configurable;
import eu.nerdfactor.restness.code.builder.MultiStepBuilder;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for builders that construct multiple methods within a controller class.
 * This class extends {@link MultiStepBuilder} and implements {@link Configurable} and {@link Buildable}
 * to provide a common structure for method builders that operate based on a {@link ControllerConfiguration}.
 * Subclasses will typically override the {@link #buildWith(TypeSpec.Builder)} method to add specific
 * methods (like CRUD operations) to the controller being built.
 * 
 * @author Daniel Klug
 */
public abstract class MultiStepMethodBuilder extends MultiStepBuilder<TypeSpec.Builder> implements Configurable<ControllerConfiguration>, Buildable<TypeSpec.Builder> {

	/**
	 * The {@link ControllerConfiguration} containing settings and type information
	 * used by the builder to generate controller methods.
	 */
	protected ControllerConfiguration configuration;

	/**
	 * Configures the builder with the provided {@link ControllerConfiguration}.
	 * This configuration provides the necessary context (entity types, DTO usage, security, etc.)
	 * for generating the controller methods.
	 *
	 * @param configuration The {@link ControllerConfiguration} to use.
	 * @return The current builder instance for fluent chaining.
	 */
	@Override
	public MultiStepMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		this.configuration = configuration;
		return this;
	}

	/**
	 * Builds the methods defined by the specific builder implementation and adds them
	 * to the provided {@link TypeSpec.Builder}.
	 * This base implementation simply returns the builder unmodified. Subclasses should
	 * override this method to add their generated methods.
	 *
	 * @param builder An existing {@link TypeSpec.Builder} representing the controller class
	 *                to which the generated methods should be added.
	 * @return The {@link TypeSpec.Builder} potentially modified with newly added methods.
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		return builder;
	}
}
