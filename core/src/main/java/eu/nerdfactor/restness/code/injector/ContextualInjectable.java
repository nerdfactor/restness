package eu.nerdfactor.restness.code.injector;

import com.squareup.javapoet.MethodSpec;

/**
 * SPI interface for custom method injectors that can be discovered via
 * {@link java.util.ServiceLoader}. Implementations are called during code
 * generation for each generated controller method, after authentication
 * injection and before the method body.
 * <p>
 * Unlike the built-in {@link Injectable}, a {@link ContextualInjectable}
 * receives a {@link MethodContext} so it can decide whether to inject
 * into a particular method.
 * <p>
 * To register a custom injector, implement this interface and add the
 * fully qualified class name to
 * {@code META-INF/services/eu.nerdfactor.restness.code.injector.ContextualInjectable}.
 *
 * @author Daniel Klug
 * @see MethodContext
 * @see MethodInjectorRegistry
 */
public interface ContextualInjectable extends Injectable<MethodSpec.Builder> {

	/**
	 * Determines whether this injector should be applied to the method
	 * described by the given context.
	 *
	 * @param context The {@link MethodContext} describing the method being generated.
	 * @return {@code true} if this injector should inject into the method.
	 */
	boolean appliesTo(MethodContext context);

	/**
	 * Returns a human-readable name for this injector, used in log messages.
	 * Defaults to the fully qualified class name.
	 *
	 * @return The name of this injector.
	 */
	default String getName() {
		return this.getClass().getCanonicalName();
	}

	/**
	 * Returns the execution order of this injector. Lower values execute first.
	 * Defaults to {@code 0}.
	 *
	 * @return The order value.
	 */
	default int getOrder() {
		return 0;
	}
}
