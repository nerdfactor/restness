package eu.nerdfactor.restness.export;

import eu.nerdfactor.restness.config.ControllerConfiguration;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
public class RestnessConfigFile {

	public Map<String, String> config;
	public Map<String, ControllerConfiguration> controllers;

}
