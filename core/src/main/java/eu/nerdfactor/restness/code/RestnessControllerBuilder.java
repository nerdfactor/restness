package eu.nerdfactor.restness.code;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.builder.Buildable;
import eu.nerdfactor.restness.code.builder.Configurable;
import eu.nerdfactor.restness.code.builder.MultiStepBuilder;
import eu.nerdfactor.restness.code.classbuilder.ClassPropertiesBuilder;
import eu.nerdfactor.restness.code.injector.MethodInjectorRegistry;
import eu.nerdfactor.restness.code.methodbuilder.*;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.util.RestnessUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class RestnessControllerBuilder extends MultiStepBuilder<TypeSpec.Builder> implements Configurable<ControllerConfiguration> {

	private static final ClassName TAG_CLASS =
			ClassName.get("io.swagger.v3.oas.annotations.tags", "Tag");

	/**
	 * The {@link ControllerConfiguration} used to create the controller.
	 */
	protected ControllerConfiguration configuration;

	/**
	 * Create a new {@link RestnessControllerBuilder}.
	 *
	 * @return A new {@link RestnessControllerBuilder}.
	 */
	public static RestnessControllerBuilder create() {
		return new RestnessControllerBuilder();
	}

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
		MethodInjectorRegistry injectorRegistry = new MethodInjectorRegistry();
		MethodBuilderRegistry builderRegistry = new MethodBuilderRegistry();

		TypeSpec.Builder builder = TypeSpec.classBuilder(configuration.getControllerClassName())
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		if (configuration.isOpenApi()) {
			String entityName = RestnessUtil.toClassName(configuration.getEntityType()).simpleName();
			builder.addAnnotation(AnnotationSpec.builder(TAG_CLASS)
					.addMember("name", "$S", entityName)
					.addMember("description", "$S", entityName + " management endpoints")
					.build());
		}

		this.and(ClassPropertiesBuilder.create().withConfiguration(this.configuration));
		this.and(CrudMethodBuilder.create().withConfiguration(this.configuration).withInjectorRegistry(injectorRegistry));
		this.and(ListMethodBuilder.create().withConfiguration(this.configuration).withInjectorRegistry(injectorRegistry));
		this.and(SearchMethodBuilder.create().withConfiguration(this.configuration).withInjectorRegistry(injectorRegistry));
		this.and(RelationshipMethodBuilder.create().withConfiguration(this.configuration).withInjectorRegistry(injectorRegistry));

		for (Buildable<TypeSpec.Builder> customBuilder : builderRegistry.createBuilders(this.configuration)) {
			this.and(customBuilder);
		}

		this.buildAll(builder);
		return builder.build();
	}
}
