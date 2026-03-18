package eu.nerdfactor.restness.example.extension;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import eu.nerdfactor.restness.code.injector.ContextualInjectable;
import eu.nerdfactor.restness.code.injector.MethodContext;

import java.util.Set;

/**
 * Example custom injector that adds a {@link RateLimited} annotation to all
 * write methods (POST, PUT, PATCH) in generated controllers.
 * <p>
 * This demonstrates how to implement the {@link ContextualInjectable} SPI
 * to inject cross-cutting concerns into generated controller methods. The
 * injector is discovered automatically via {@link java.util.ServiceLoader}
 * from the {@code META-INF/services} registration file.
 * <p>
 * In a production application, you might use this pattern to add annotations
 * for rate limiting, caching, logging, or custom validation.
 *
 * @author Daniel Klug
 * @see ContextualInjectable
 * @see RateLimited
 */
public class RateLimitedInjector implements ContextualInjectable {

	private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH");

	private static final ClassName RATE_LIMITED_CLASS =
			ClassName.get("eu.nerdfactor.restness.example.extension", "RateLimited");

	/**
	 * Applies only to write methods (POST, PUT, PATCH). Read (GET) and
	 * delete (DELETE) methods are not rate-limited by this injector.
	 *
	 * @param context The method generation context.
	 * @return {@code true} if the method is a write operation.
	 */
	@Override
	public boolean appliesTo(MethodContext context) {
		return WRITE_METHODS.contains(context.getHttpMethod());
	}

	/**
	 * Adds {@code @RateLimited(requestsPerMinute = 60)} to the method.
	 *
	 * @param builder The method builder to inject into.
	 * @return The modified method builder.
	 */
	@Override
	public MethodSpec.Builder inject(MethodSpec.Builder builder) {
		builder.addAnnotation(AnnotationSpec.builder(RATE_LIMITED_CLASS)
				.addMember("requestsPerMinute", "$L", 60)
				.build());
		return builder;
	}

	@Override
	public String getName() {
		return "rate-limited";
	}

	@Override
	public int getOrder() {
		return 10;
	}
}
