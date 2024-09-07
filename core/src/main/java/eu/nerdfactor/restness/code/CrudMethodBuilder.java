package eu.nerdfactor.restness.code;

import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.builder.Buildable;
import eu.nerdfactor.restness.code.builder.Configurable;
import eu.nerdfactor.restness.code.builder.MultiStepBuilder;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import org.jetbrains.annotations.NotNull;

public class CrudMethodBuilder extends MultiStepBuilder<TypeSpec.Builder> implements Configurable<ControllerConfiguration>, Buildable<TypeSpec.Builder> {

	protected ControllerConfiguration configuration;

	@Override
	public CrudMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		this.configuration = configuration;
		return this;
	}

	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		this.and(CreateEntityMethodBuilder.create().withConfiguration(this.configuration));
		this.and(ReadEntityMethodBuilder.create().withConfiguration(this.configuration));
		this.and(UpdateEntityMethodBuilder.create().withConfiguration(this.configuration));
		this.and(SetEntityMethodBuilder.create().withConfiguration(this.configuration));
		this.and(DeleteEntityMethodBuilder.create().withConfiguration(this.configuration));
		this.steps.forEach(buildStep -> buildStep.build(builder));
		return builder;
	}

}
