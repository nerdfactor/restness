package eu.nerdfactor.restness.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.nerdfactor.restness.config.ControllerConfiguration;

import javax.annotation.processing.Filer;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class JsonConfigExporter implements RestnessExporter {

	public JsonConfigExporter withFiler(Filer filer) {
		return this;
	}

	@Override
	public void export(Map<String, String> config, Map<String, ControllerConfiguration> controllers) {
		RestnessConfigFile file = new RestnessConfigFile();
		file.config = config;
		file.controllers = controllers;
		ObjectMapper mapper = ConfigMapper.forJson();
		try {
			mapper.writeValue(new File("restness.json"), file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
