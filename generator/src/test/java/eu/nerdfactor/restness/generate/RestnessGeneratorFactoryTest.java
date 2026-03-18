package eu.nerdfactor.restness.generate;

import eu.nerdfactor.restness.export.JsonConfigExporter;
import eu.nerdfactor.restness.export.YamlConfigExporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RestnessGeneratorFactoryTest {

	private RestnessGeneratorFactory factory;

	@BeforeEach
	void setUp() {
		this.factory = new RestnessGeneratorFactory();
	}

	@Test
	void shouldDiscoverJavaGeneratorByShortName() {
		RestnessGenerator generator = this.factory.getGenerator("java");
		assertInstanceOf(JavaClassGenerator.class, generator);
	}

	@Test
	void shouldDiscoverJsonExporterByShortName() {
		RestnessGenerator generator = this.factory.getGenerator("json");
		assertInstanceOf(JsonConfigExporter.class, generator);
	}

	@Test
	void shouldDiscoverYamlExporterByShortName() {
		RestnessGenerator generator = this.factory.getGenerator("yaml");
		assertInstanceOf(YamlConfigExporter.class, generator);
	}

	@Test
	void shouldDiscoverGeneratorByFullyQualifiedClassName() {
		RestnessGenerator generator = this.factory.getGenerator("eu.nerdfactor.restness.generate.JavaClassGenerator");
		assertInstanceOf(JavaClassGenerator.class, generator);
	}

	@Test
	void shouldDiscoverJsonExporterByFullyQualifiedClassName() {
		RestnessGenerator generator = this.factory.getGenerator("eu.nerdfactor.restness.export.JsonConfigExporter");
		assertInstanceOf(JsonConfigExporter.class, generator);
	}

	@Test
	void shouldFallbackToJavaClassGeneratorForUnknownName() {
		RestnessGenerator generator = this.factory.getGenerator("nonexistent");
		assertInstanceOf(JavaClassGenerator.class, generator);
	}

	@Test
	void shouldReturnCorrectShortNames() {
		assertEquals("java", new JavaClassGenerator().getName());
		assertEquals("json", new JsonConfigExporter().getName());
		assertEquals("yaml", new YamlConfigExporter().getName());
	}
}
