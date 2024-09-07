package eu.nerdfactor.restness.code;

import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.builder.Configurable;
import eu.nerdfactor.restness.code.builder.MultiStepBuilder;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;

public class RestnessControllerBuilder extends MultiStepBuilder<TypeSpec.Builder> implements Configurable<ControllerConfiguration> {

	ControllerConfiguration configuration;

	@Override
	public RestnessControllerBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		this.configuration = configuration;
		return this;
	}

	public TypeSpec build() {
		TypeSpec.Builder builder = TypeSpec.classBuilder(configuration.getClassName()).addAnnotation(RestController.class).addModifiers(Modifier.PUBLIC);
		this.and(new ClassPropertiesBuilder().withConfiguration(this.configuration));
		this.and(new CrudMethodBuilder().withConfiguration(this.configuration));
		this.and(new ListMethodBuilder().withConfiguration(this.configuration));
		this.and(new SearchMethodBuilder().withConfiguration(this.configuration));
		this.and(new RelationshipMethodBuilder().withConfiguration(this.configuration));
		this.steps.forEach(buildStep -> buildStep.build(builder));
		return builder.build();
	}
}
