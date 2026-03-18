package eu.nerdfactor.restness.code.injector;

import com.squareup.javapoet.MethodSpec;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Registry that discovers {@link ContextualInjectable} implementations via
 * {@link ServiceLoader} and dispatches injection calls during code generation.
 * <p>
 * Discovered injectors are sorted by {@link ContextualInjectable#getOrder()}
 * and called in that order for each generated method. An injector is only
 * invoked if its {@link ContextualInjectable#appliesTo(MethodContext)} returns
 * {@code true} for the given context.
 *
 * @author Daniel Klug
 * @see ContextualInjectable
 * @see MethodContext
 */
@Slf4j
public class MethodInjectorRegistry {

	/**
	 * The discovered and sorted list of custom injectors.
	 */
	private final List<ContextualInjectable> injectors;

	/**
	 * Creates a new registry by discovering all {@link ContextualInjectable}
	 * implementations on the classpath via {@link ServiceLoader}.
	 */
	public MethodInjectorRegistry() {
		this.injectors = new ArrayList<>();
		ServiceLoader.load(ContextualInjectable.class, ContextualInjectable.class.getClassLoader()).forEach(injector -> {
			this.injectors.add(injector);
			log.debug("Discovered custom injector '{}'.", injector.getName());
		});
		this.injectors.sort(Comparator.comparingInt(ContextualInjectable::getOrder));
	}

	/**
	 * Creates a new registry with the given injectors. This constructor
	 * bypasses {@link ServiceLoader} discovery and is useful for programmatic
	 * configuration or testing.
	 *
	 * @param injectors The injectors to register, sorted by {@link ContextualInjectable#getOrder()}.
	 */
	public MethodInjectorRegistry(List<ContextualInjectable> injectors) {
		this.injectors = new ArrayList<>(injectors);
		this.injectors.sort(Comparator.comparingInt(ContextualInjectable::getOrder));
	}

	/**
	 * Applies all matching custom injectors to the given method builder.
	 *
	 * @param method  The {@link MethodSpec.Builder} to inject into.
	 * @param context The {@link MethodContext} describing the method being generated.
	 * @return The potentially modified {@link MethodSpec.Builder}.
	 */
	public MethodSpec.Builder injectAll(MethodSpec.Builder method, MethodContext context) {
		for (ContextualInjectable injector : this.injectors) {
			if (injector.appliesTo(context)) {
				method = injector.inject(method);
			}
		}
		return method;
	}

	/**
	 * Returns whether any custom injectors were discovered.
	 *
	 * @return {@code true} if at least one injector is registered.
	 */
	public boolean hasInjectors() {
		return !this.injectors.isEmpty();
	}
}
