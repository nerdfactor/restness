package eu.nerdfactor.restness.export;

import com.squareup.javapoet.JavaFile;
import eu.nerdfactor.restness.code.RestnessControllerBuilder;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.util.RestnessUtil;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.util.Map;

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
				RestnessUtil.log("Generating " + controllerConfiguration.getControllerClassName().canonicalName() + " for " + controllerConfiguration.getEntityClassName().toString() + ".");
				JavaFile.builder(
								controllerConfiguration.getControllerClassName().packageName(),
								RestnessControllerBuilder.create().withConfiguration(controllerConfiguration).build()
						).indent(config.getOrDefault("indentation", "\t"))
						.build()
						.writeTo(filer);
			} catch (IOException e) {
				RestnessUtil.log("Could not generate " + controllerConfiguration.getControllerClassName().canonicalName() + ".");
				e.printStackTrace();
			}
		});
	}
}
