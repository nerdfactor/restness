package eu.nerdfactor.restness.code;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.builder.*;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.data.DataSpecificationBuilder;
import jakarta.persistence.EntityManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.util.Pair;

import java.util.List;

public class GeneratedPropertiesBuilder extends MultiStepBuilder<TypeSpec.Builder> implements Buildable<TypeSpec.Builder>, Configurable<ControllerConfiguration> {

	ControllerConfiguration configuration;

	@Override
	public GeneratedPropertiesBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		this.configuration = configuration;
		return this;
	}

	public TypeSpec.Builder build(TypeSpec.Builder builder) {
		ConstructorBuilder constructor = new ConstructorBuilder();
		List.of(
				Pair.of("dataAccessor", (TypeName) configuration.getDataAccessorClass()),
				Pair.of("dataMapper", configuration.getDataMapperClass()),
				Pair.of("dataMerger", configuration.getDataMergerClass()),
				Pair.of("specificationBuilder", (TypeName) ClassName.get(DataSpecificationBuilder.class)),
				Pair.of("entityManager", (TypeName) ClassName.get(EntityManager.class))
		).forEach(pair -> {
			this.and(new PropertyBuilder().withProperty(pair));
			constructor.withProperty(pair);
		});
		this.and(constructor);
		this.steps.forEach(buildStep -> buildStep.build(builder));
		return builder;
	}
}
