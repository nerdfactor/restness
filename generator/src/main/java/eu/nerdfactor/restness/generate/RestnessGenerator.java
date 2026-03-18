package eu.nerdfactor.restness.generate;

import eu.nerdfactor.restness.config.ControllerConfiguration;

import javax.annotation.processing.Filer;
import java.util.Map;

public interface RestnessGenerator {

	/**
	 * Returns the name of this generator. Used for discovery and lookup.
	 * Built-in generators return short names (e.g. "java", "json", "yaml").
	 * External implementations default to their fully qualified class name.
	 *
	 * @return The name of this generator.
	 */
	default String getName() {
		return this.getClass().getCanonicalName();
	}

	public RestnessGenerator withFiler(Filer filer);

	public void generate(Map<String, String> config, Map<String, ControllerConfiguration> controllers);
}
