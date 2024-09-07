package eu.nerdfactor.restness.code;

import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.builder.Configurable;
import eu.nerdfactor.restness.code.builder.MultiStepBuilder;
import eu.nerdfactor.restness.code.classbuilder.ClassPropertiesBuilder;
import eu.nerdfactor.restness.code.methodbuilder.CrudMethodBuilder;
import eu.nerdfactor.restness.code.methodbuilder.ListMethodBuilder;
import eu.nerdfactor.restness.code.methodbuilder.RelationshipMethodBuilder;
import eu.nerdfactor.restness.code.methodbuilder.SearchMethodBuilder;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;

/**
 * A builder that can be used to create a RESTness controller. It will use a set
 * of additional builders that in turn each build a small part of the
 * controller.
 * <p>
 * A RESTness controller consist of:
 * <li>A Spring RestController class.</li>
 * <li>Fields for entity data access.</li>
 * <li>Methods for CRUD access to the entity.</li>
 * <li>Methods to list the entity.</li>
 * <li>Methods to search for the entity.</li>
 * <li>Methods to manage related entities.</li>
 *
 * @author Daniel Klug
 */
public class RestnessControllerBuilder extends MultiStepBuilder<TypeSpec.Builder> implements Configurable<ControllerConfiguration> {

	/**
	 * The {@link ControllerConfiguration} used to create the controller.
	 */
	protected ControllerConfiguration configuration;

	/**
	 * Set the {@link ControllerConfiguration} that will be used.
	 *
	 * @param configuration The {@link ControllerConfiguration}.
	 * @return The builder in a fluent api pattern.
	 */
	@Override
	public RestnessControllerBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		this.configuration = configuration;
		return this;
	}

	/**
	 * Build a {@link TypeSpec} for a RESTness controller.
	 *
	 * @return The build {@link TypeSpec}.
	 */
	public TypeSpec build() {
		TypeSpec.Builder builder = TypeSpec.classBuilder(configuration.getControllerClassName())
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);
		this.and(new ClassPropertiesBuilder().withConfiguration(this.configuration));
		this.and(new CrudMethodBuilder().withConfiguration(this.configuration));
		this.and(new ListMethodBuilder().withConfiguration(this.configuration));
		this.and(new SearchMethodBuilder().withConfiguration(this.configuration));
		this.and(new RelationshipMethodBuilder().withConfiguration(this.configuration));
		this.buildAll(builder);
		return builder.build();
	}
}
