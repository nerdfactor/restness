package eu.nerdfactor.restness.export;

import eu.nerdfactor.restness.config.ControllerConfiguration;

import java.util.Map;

public class RestnessConfigFile {

	public Map<String, String> config;
	public Map<String, ControllerConfiguration> controllers;

	public RestnessConfigFile() {
	}
}
