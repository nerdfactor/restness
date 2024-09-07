package eu.nerdfactor.restness.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.nerdfactor.restness.config.ControllerConfiguration;

import javax.annotation.processing.Filer;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class YamlConfigExporter implements GeneratedRestExporter {

	public YamlConfigExporter withFiler(Filer filer) {
		return this;
	}

	@Override
	public void export(Map<String, String> config, Map<String, ControllerConfiguration> controllers) {
		GeneratedConfigFile file = new GeneratedConfigFile();
		file.config = config;
		file.controllers = controllers;
		ObjectMapper mapper = ConfigMapper.forYaml();
		try {
			mapper.writeValue(new File("generated-rest.yaml"), file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
