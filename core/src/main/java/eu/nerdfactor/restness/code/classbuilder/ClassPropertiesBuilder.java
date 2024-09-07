package eu.nerdfactor.restness.code.classbuilder;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.builder.*;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.data.DataSpecificationBuilder;
import jakarta.persistence.EntityManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A builder that creates all required class properties in a controller.
 *
 * @author Daniel Klug
 */
public class ClassPropertiesBuilder extends MultiStepBuilder<TypeSpec.Builder> implements Buildable<TypeSpec.Builder>, Configurable<ControllerConfiguration> {

	/**
	 * The configuration used to create the controller.
	 */
	protected ControllerConfiguration configuration;

	/**
	 * Set the {@link ControllerConfiguration} that will be used to build.
	 *
	 * @param configuration The {@link ControllerConfiguration}.
	 * @return The builder in a fluent api pattern.
	 */
	@Override
	public ClassPropertiesBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		this.configuration = configuration;
		return this;
	}

	/**
	 * Create a {@link TypeSpec.Builder} containing all required class
	 * properties and a constructor.
	 *
	 * @param builder An existing builder object that will be appended.
	 * @return The build {@link TypeSpec.Builder}.
	 */
	public TypeSpec.Builder buildWith(final TypeSpec.Builder builder) {
		final ConstructorBuilder constructor = new ConstructorBuilder();
		List.of(
				new PropertyPair("dataAccessor", configuration.getDataAccessorClassName()),
				new PropertyPair("dataMapper", configuration.getDataMapperClassName()),
				new PropertyPair("dataMerger", configuration.getDataMergerClassName()),
				new PropertyPair("specificationBuilder", ClassName.get(DataSpecificationBuilder.class)),
				new PropertyPair("entityManager", ClassName.get(EntityManager.class))
		).forEach(pair -> {
			this.and(new PropertyBuilder().withProperty(pair.name(), pair.type()));
			constructor.withProperty(pair.name(), pair.type());
		});
		this.and(constructor);
		return this.buildAll(builder);
	}
}
