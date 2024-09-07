package eu.nerdfactor.restness.code.injector;

import com.squareup.javapoet.MethodSpec;
import org.springframework.http.ResponseEntity;

public class NoContentStatementInjector extends ReturnStatementInjector {

	@Override
	protected void addWrapperContent(MethodSpec.Builder builder) {
		builder.addStatement("wrapper.noContent()");
	}

	@Override
	protected void addBasicReturn(MethodSpec.Builder builder) {
		builder.addStatement("return ResponseEntity.noContent().build()", ResponseEntity.class);
	}
}
