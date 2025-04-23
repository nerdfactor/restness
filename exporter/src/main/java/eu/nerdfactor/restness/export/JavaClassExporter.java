package eu.nerdfactor.restness.export;

import com.squareup.javapoet.JavaFile;
import eu.nerdfactor.restness.code.RestnessControllerBuilder;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.util.Map;

@Slf4j
public class JavaClassExporter implements RestnessExporter {

	private Filer filer;

	public JavaClassExporter withFiler(Filer filer) {
		this.filer = filer;
		return this;
	}


	@Override
	public void export(Map<String, String> config, Map<String, ControllerConfiguration> controllers) {
		controllers.values().forEach(controllerConfiguration -> {
			try {
				log.info("Generating {} for {}.", controllerConfiguration.getControllerClassName().canonicalName(), controllerConfiguration.getEntityClassName().toString());
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
