package eu.nerdfactor.restness.export;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigImporterTest {

	@Test
	void shouldImportFromJsonFile(@TempDir Path tempDir) throws Exception {
		String json = """
				{
				  "config": {
				    "classNamePrefix": "Generated",
				    "indentation": "    "
				  },
				  "controllers": {
				    "ProductController": {
				      "controllerClassName": "com.example.ProductController",
				      "requestBasePath": "/api/products",
				      "entityType": "com.example.Product",
				      "idType": "java.lang.Integer",
				      "idAccessorMethodName": "getId",
				      "idModifierMethodName": "setId"
				    }
				  }
				}
				""";
		File jsonFile = tempDir.resolve("restness.json").toFile();
		java.nio.file.Files.writeString(jsonFile.toPath(), json);

		ConfigImporter importer = new ConfigImporter();
		RestnessConfigFile configFile = importer.importFromFile(jsonFile.getAbsolutePath());

		assertNotNull(configFile);
		assertNotNull(configFile.config);
		assertEquals("Generated", configFile.config.get("classNamePrefix"));
		assertEquals("    ", configFile.config.get("indentation"));
		assertNotNull(configFile.controllers);
		assertTrue(configFile.controllers.containsKey("ProductController"));
		assertEquals("/api/products", configFile.controllers.get("ProductController").getRequestBasePath());
	}

	@Test
	void shouldImportFromYamlFile(@TempDir Path tempDir) throws Exception {
		String yaml = """
				config:
				  classNamePrefix: Generated
				  indentation: "    "
				controllers:
				  OrderController:
				    controllerClassName: com.example.OrderController
				    requestBasePath: /api/orders
				    entityType: com.example.Order
				    idType: java.lang.Integer
				    idAccessorMethodName: getId
				    idModifierMethodName: setId
				""";
		File yamlFile = tempDir.resolve("restness.yaml").toFile();
		java.nio.file.Files.writeString(yamlFile.toPath(), yaml);

		ConfigImporter importer = new ConfigImporter();
		RestnessConfigFile configFile = importer.importFromFile(yamlFile.getAbsolutePath());

		assertNotNull(configFile);
		assertNotNull(configFile.controllers);
		assertTrue(configFile.controllers.containsKey("OrderController"));
		assertEquals("/api/orders", configFile.controllers.get("OrderController").getRequestBasePath());
	}

	@Test
	void shouldReturnEmptyConfigForNonExistentFile() {
		ConfigImporter importer = new ConfigImporter();
		RestnessConfigFile configFile = importer.importFromFile("nonexistent.json");

		assertNotNull(configFile);
	}
}
