package eu.nerdfactor.restness.export;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class ConfigImporter {

	public RestnessConfigFile importFromFile(String path) {
		ObjectMapper mapper = ConfigMapper.forFile(path);
		try {
			return mapper.readValue(Path.of(path).toFile(), RestnessConfigFile.class);
		} catch (Exception e) {
			return new RestnessConfigFile();
		}
	}

	public RestnessConfigFile importFromFile(String path, String schemaPath) throws IOException {
		ObjectMapper mapper = ConfigMapper.forFile(path);
		JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
		JsonNode json = mapper.readTree(Files.readString(Path.of(path)));
		JsonSchema schema = schemaFactory.getSchema(Files.readString(Path.of(schemaPath)));
		Set<ValidationMessage> validationResult = schema.validate(json);
		if (validationResult.isEmpty()) {
			return this.importFromFile(path);
		} else {
			throw new RuntimeException("Config file is invalid.");
		}
	}

}
