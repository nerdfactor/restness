package eu.nerdfactor.restness.export;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.restness.serialization.*;

import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.SPLIT_LINES;

public class ConfigMapper extends ObjectMapper {

	public static ObjectMapper forFile(String path) {
		if (path.endsWith(".json")) {
			return forJson();
		}
		if (path.endsWith(".yml") || path.endsWith(".yaml")) {
			return forYaml();
		}
		return new ConfigMapper();
	}

	public static ObjectMapper forJson() {
		ObjectMapper mapper = new JsonMapper();
		mapper.registerModule(createMappingModule());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}

	public static ObjectMapper forYaml() {
		YAMLFactory yamlFactory = YAMLFactory.builder()
				.disable(SPLIT_LINES)
				.build();
		ObjectMapper mapper = new ObjectMapper(yamlFactory);
		mapper.registerModule(createMappingModule());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}

	private static SimpleModule createMappingModule() {
		SimpleModule module = new SimpleModule();
		module.addSerializer(ClassName.class, new ClassNameSerializer());
		module.addDeserializer(ClassName.class, new ClassNameDeserializer());
		module.addSerializer(TypeName.class, new TypeNameSerializer());
		module.addDeserializer(TypeName.class, new TypeNameDeserializer());
		module.addSerializer(ParameterizedTypeName.class, new ParameterizedTypeNameSerializer());
		module.addDeserializer(ParameterizedTypeName.class, new ParameterizedTypeNameDeserializer());
		return module;
	}
}
