package eu.nerdfactor.restness.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.generate.RestnessGenerator;

import javax.annotation.processing.Filer;
import java.io.File;
import java.io.IOException;
import java.util.Map;

@AutoService(RestnessGenerator.class)
public class YamlConfigExporter implements RestnessGenerator {

	private static final String NAME = "yaml";

	@Override
	public String getName() {
		return NAME;
	}

	public YamlConfigExporter withFiler(Filer filer) {
		return this;
	}

	@Override
	public void generate(Map<String, String> config, Map<String, ControllerConfiguration> controllers) {
		RestnessConfigFile file = new RestnessConfigFile();
		file.config = config;
		file.controllers = controllers;
		ObjectMapper mapper = ConfigMapper.forYaml();
		try {
			mapper.writeValue(new File("restness.yaml"), file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
