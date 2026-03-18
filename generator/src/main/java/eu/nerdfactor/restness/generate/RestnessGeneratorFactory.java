package eu.nerdfactor.restness.generate;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Factory for creating {@link RestnessGenerator} instances using Java's
 * {@link ServiceLoader} SPI mechanism. Generators are discovered automatically
 * from the classpath and can be resolved by their short name (e.g. "java")
 * or by their fully qualified class name.
 *
 * @author Daniel Klug
 * @see RestnessGenerator#getName()
 */
@Slf4j
public class RestnessGeneratorFactory {

	private final Map<String, RestnessGenerator> generators;

	public RestnessGeneratorFactory() {
		this.generators = new LinkedHashMap<>();
		ServiceLoader.load(RestnessGenerator.class).forEach(generator -> {
			this.generators.put(generator.getName(), generator);
			log.debug("Discovered generator '{}' ({}).", generator.getName(), generator.getClass().getCanonicalName());
		});
	}

	/**
	 * Resolves a {@link RestnessGenerator} by name or fully qualified class name.
	 * <p>
	 * Resolution order:
	 * <ol>
	 *     <li>Direct match by registered name (handles both short names and FQCN-as-default-name)</li>
	 *     <li>Match by actual class canonical name (for generators with short names requested by FQCN)</li>
	 *     <li>Fallback to {@link JavaClassGenerator}</li>
	 * </ol>
	 *
	 * @param nameOrClassName The short name or fully qualified class name of the generator.
	 * @return The resolved generator instance, never null.
	 */
	public RestnessGenerator getGenerator(String nameOrClassName) {
		RestnessGenerator found = this.generators.get(nameOrClassName);
		if (found != null) {
			return found;
		}

		for (RestnessGenerator generator : this.generators.values()) {
			if (generator.getClass().getCanonicalName().equals(nameOrClassName)) {
				return generator;
			}
		}

		log.warn("Generator '{}' not found. Falling back to default JavaClassGenerator.", nameOrClassName);
		return this.generators.values().stream()
				.filter(JavaClassGenerator.class::isInstance)
				.findFirst()
				.orElseGet(JavaClassGenerator::new);
	}
}
