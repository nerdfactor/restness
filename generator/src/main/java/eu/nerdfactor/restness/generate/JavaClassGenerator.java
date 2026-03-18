package eu.nerdfactor.restness.generate;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import eu.nerdfactor.restness.code.RestnessControllerBuilder;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.util.Map;

@Slf4j
@AutoService(RestnessGenerator.class)
public class JavaClassGenerator implements RestnessGenerator {

	private static final String NAME = "java";

	private Filer filer;

	@Override
	public String getName() {
		return NAME;
	}

	public JavaClassGenerator withFiler(Filer filer) {
		this.filer = filer;
		return this;
	}


	@Override
	public void generate(Map<String, String> config, Map<String, ControllerConfiguration> controllers) {
		boolean openApi = Boolean.parseBoolean(config.getOrDefault("openApi", "false"));
		controllers.values().forEach(controllerConfiguration -> {
			controllerConfiguration.setOpenApi(openApi);
			try {
				log.info("Generating {} for {}.", controllerConfiguration.getControllerClassName().canonicalName(), controllerConfiguration.getEntityType().toString());
				JavaFile.builder(
								controllerConfiguration.getControllerClassName().packageName(),
								RestnessControllerBuilder.create().withConfiguration(controllerConfiguration).build()
						).indent(config.getOrDefault("indentation", "\t"))
						.build()
						.writeTo(filer);
			} catch (IOException e) {
				log.error("Could not generate {}.", controllerConfiguration.getControllerClassName().canonicalName());
				e.printStackTrace();
			}
		});
	}
}
