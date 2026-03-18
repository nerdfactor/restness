package eu.nerdfactor.restness.example.extension;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.builder.Buildable;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.util.RestnessUtil;

import javax.lang.model.element.Modifier;

/**
 * Example custom method builder that adds a {@code count()} endpoint to
 * generated controllers. The generated method returns the total number of
 * entities available via the data accessor.
 * <p>
 * This demonstrates how to implement {@link Buildable} to add entirely new
 * methods to generated controllers. The builder receives the
 * {@link ControllerConfiguration} so it can adapt to the entity's base path
 * and types.
 * <p>
 * Generated endpoint example:
 * <pre>
 * {@code GET /api/products/count → ResponseEntity<Long>}
 * </pre>
 *
 * @author Daniel Klug
 * @see CountEndpointBuilderProvider
 */
public class CountEndpointBuilder implements Buildable<TypeSpec.Builder> {

	private static final ClassName GET_MAPPING = ClassName.get("org.springframework.web.bind.annotation", "GetMapping");
	private static final ClassName RESPONSE_STATUS = ClassName.get("org.springframework.web.bind.annotation", "ResponseStatus");
	private static final ClassName HTTP_STATUS = ClassName.get("org.springframework.http", "HttpStatus");
	private static final ClassName RESPONSE_ENTITY = ClassName.get("org.springframework.http", "ResponseEntity");
	private static final ClassName PAGEABLE = ClassName.get("org.springframework.data.domain", "Pageable");

	private final String basePath;
	private final TypeName entityType;

	CountEndpointBuilder(String basePath, TypeName entityType) {
		this.basePath = basePath;
		this.entityType = entityType;
	}

	/**
	 * Creates a new builder configured from the given controller configuration.
	 *
	 * @param configuration The controller configuration.
	 * @return A configured builder instance.
	 */
	static CountEndpointBuilder fromConfiguration(ControllerConfiguration configuration) {
		return new CountEndpointBuilder(
				configuration.getRequestBasePath(),
				configuration.getEntityType()
		);
	}

	/**
	 * Adds a {@code count()} method to the controller that returns the total
	 * number of entities. Uses {@code dataAccessor.searchData(null, Pageable.ofSize(1)).getTotalElements()}
	 * to efficiently retrieve the count without loading all entities.
	 *
	 * @param builder The controller class builder.
	 * @return The modified class builder with the count method added.
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		String entityName = RestnessUtil.toClassName(this.entityType).simpleName();
		String path = this.basePath + "/count";

		MethodSpec.Builder method = MethodSpec.methodBuilder("count" + entityName)
				.addAnnotation(AnnotationSpec.builder(GET_MAPPING)
						.addMember("value", "$S", path)
						.build())
				.addAnnotation(AnnotationSpec.builder(RESPONSE_STATUS)
						.addMember("value", "$T.$L", HTTP_STATUS, "OK")
						.build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(RESPONSE_ENTITY, ClassName.get(Long.class)));

		method.addStatement("long count = this.dataAccessor.searchData(null, $T.ofSize(1)).getTotalElements()", PAGEABLE);
		method.addStatement("return new $T<>(count, $T.OK)", RESPONSE_ENTITY, HTTP_STATUS);

		builder.addMethod(method.build());
		return builder;
	}
}
