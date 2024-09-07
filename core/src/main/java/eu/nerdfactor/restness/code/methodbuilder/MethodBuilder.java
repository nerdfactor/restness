package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.builder.Buildable;
import eu.nerdfactor.restness.code.builder.Configurable;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import org.jetbrains.annotations.NotNull;

public class MethodBuilder implements Buildable<TypeSpec.Builder>, Configurable<ControllerConfiguration> {

	protected ControllerConfiguration configuration;

	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		return builder;
	}

	@Override
	public MethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		this.configuration = configuration;
		return this;
	}
}
