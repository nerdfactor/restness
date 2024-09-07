package eu.nerdfactor.restness.export;

import com.squareup.javapoet.JavaFile;
import eu.nerdfactor.restness.code.GeneratedControllerBuilder;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.util.GeneratedRestUtil;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.util.Map;

public class JavaClassExporter implements GeneratedRestExporter {

	private Filer filer;

	public JavaClassExporter withFiler(Filer filer) {
		this.filer = filer;
		return this;
	}


	@Override
	public void export(Map<String, String> config, Map<String, ControllerConfiguration> controllers) {
		controllers.values().forEach(controllerConfiguration -> {
			try {
				GeneratedRestUtil.log("Generating " + controllerConfiguration.getClassName().canonicalName() + " for " + controllerConfiguration.getEntity().toString() + ".");
				JavaFile.builder(
								controllerConfiguration.getClassName().packageName(),
								new GeneratedControllerBuilder().withConfiguration(controllerConfiguration).build()
						).indent(config.getOrDefault("indentation", "\t"))
						.build()
						.writeTo(filer);
			} catch (IOException e) {
				GeneratedRestUtil.log("Could not generate " + controllerConfiguration.getClassName().canonicalName() + ".");
				e.printStackTrace();
			}
		});
	}
}
