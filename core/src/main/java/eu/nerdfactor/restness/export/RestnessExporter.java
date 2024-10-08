package eu.nerdfactor.restness.export;

import eu.nerdfactor.restness.config.ControllerConfiguration;

import javax.annotation.processing.Filer;
import java.util.Map;

public interface RestnessExporter {

	public RestnessExporter withFiler(Filer filer);

	public void export(Map<String, String> config, Map<String, ControllerConfiguration> controllers);
}
