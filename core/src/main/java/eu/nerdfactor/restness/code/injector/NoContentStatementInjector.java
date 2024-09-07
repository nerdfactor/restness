package eu.nerdfactor.restness.code.injector;

import com.squareup.javapoet.MethodSpec;
import org.springframework.http.ResponseEntity;

/**
 * An injector that adds an empty return statement to a method.
 *
 * @author Daniel Klug
 */
public class NoContentStatementInjector extends ReturnStatementInjector {

	/**
	 * Add a statement that returns an empty wrapper object.
	 *
	 * @param builder An existing builder object that will be used.
	 */
	@Override
	protected void addWrapperContent(MethodSpec.Builder builder) {
		builder.addStatement("wrapper.noContent()");
	}

	/**
	 * Add a statement that returns an empty response.
	 *
	 * @param builder An existing builder object that will be used.
	 */
	@Override
	protected void addBasicReturn(MethodSpec.Builder builder) {
		builder.addStatement("return ResponseEntity.noContent().build()", ResponseEntity.class);
	}
}
