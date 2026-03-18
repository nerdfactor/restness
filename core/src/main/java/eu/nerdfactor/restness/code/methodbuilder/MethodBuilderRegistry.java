package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.builder.Buildable;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Registry that discovers {@link RestnessMethodBuilderProvider} implementations
 * via {@link ServiceLoader} and creates configured builders for the controller
 * generation pipeline.
 * <p>
 * Discovered providers are sorted by {@link RestnessMethodBuilderProvider#getOrder()}
 * and their builders are added after the built-in builders in the generation sequence.
 *
 * @author Daniel Klug
 * @see RestnessMethodBuilderProvider
 */
@Slf4j
public class MethodBuilderRegistry {

	/**
	 * The discovered and sorted list of custom method builder providers.
	 */
	private final List<RestnessMethodBuilderProvider> providers;

	/**
	 * Creates a new registry by discovering all {@link RestnessMethodBuilderProvider}
	 * implementations on the classpath via {@link ServiceLoader}.
	 */
	public MethodBuilderRegistry() {
		this.providers = new ArrayList<>();
		ServiceLoader.load(RestnessMethodBuilderProvider.class, RestnessMethodBuilderProvider.class.getClassLoader()).forEach(provider -> {
			this.providers.add(provider);
			log.debug("Discovered custom method builder provider '{}'.", provider.getName());
		});
		this.providers.sort(Comparator.comparingInt(RestnessMethodBuilderProvider::getOrder));
	}

	/**
	 * Creates a new registry with the given providers. This constructor
	 * bypasses {@link ServiceLoader} discovery and is useful for programmatic
	 * configuration or testing.
	 *
	 * @param providers The providers to register, sorted by {@link RestnessMethodBuilderProvider#getOrder()}.
	 */
	public MethodBuilderRegistry(List<RestnessMethodBuilderProvider> providers) {
		this.providers = new ArrayList<>(providers);
		this.providers.sort(Comparator.comparingInt(RestnessMethodBuilderProvider::getOrder));
	}

	/**
	 * Creates configured builder instances from all discovered providers.
	 *
	 * @param configuration The {@link ControllerConfiguration} for the controller being generated.
	 * @return A list of configured builders, sorted by provider order.
	 */
	public List<Buildable<TypeSpec.Builder>> createBuilders(ControllerConfiguration configuration) {
		return this.providers.stream()
				.map(provider -> provider.createBuilder(configuration))
				.toList();
	}
}
