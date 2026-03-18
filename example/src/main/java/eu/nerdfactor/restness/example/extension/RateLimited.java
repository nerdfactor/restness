package eu.nerdfactor.restness.example.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller method as rate-limited. This annotation is added to
 * generated write methods (POST, PUT, PATCH) by the {@link RateLimitedInjector}
 * custom SPI injector.
 * <p>
 * In a production application, this annotation would be processed by an AOP
 * aspect that enforces the configured rate limit. Here it serves as an example
 * of how custom annotations can be injected into generated controller methods
 * via the {@link eu.nerdfactor.restness.code.injector.ContextualInjectable} SPI.
 *
 * @author Daniel Klug
 * @see RateLimitedInjector
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

	/**
	 * The maximum number of requests allowed per minute.
	 *
	 * @return The rate limit in requests per minute.
	 */
	int requestsPerMinute() default 60;
}
