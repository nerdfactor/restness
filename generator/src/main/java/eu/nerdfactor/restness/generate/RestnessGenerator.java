package eu.nerdfactor.restness.generate;

import eu.nerdfactor.restness.config.ControllerConfiguration;

import javax.annotation.processing.Filer;
import java.util.Map;

public interface RestnessGenerator {

	public RestnessGenerator withFiler(Filer filer);

	public void generate(Map<String, String> config, Map<String, ControllerConfiguration> controllers);
}
