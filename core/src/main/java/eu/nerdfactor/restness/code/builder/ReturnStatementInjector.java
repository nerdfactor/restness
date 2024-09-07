package eu.nerdfactor.restness.code.builder;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ReturnStatementInjector implements Injectable<MethodSpec.Builder> {

	protected TypeName wrapperType;

	protected TypeName responseType;

	protected String responseVariableName = "response";

	public ReturnStatementInjector withWrapper(TypeName wrapper) {
		this.wrapperType = wrapper;
		return this;
	}

	public ReturnStatementInjector withResponse(TypeName response) {
		this.responseType = response;
		return this;
	}

	public ReturnStatementInjector withResponseVariable(String variable) {
		this.responseVariableName = variable;
		return this;
	}

	public MethodSpec.Builder inject(MethodSpec.Builder builder) {
		if (wrapperType != null && !wrapperType.equals(TypeName.OBJECT)) {
			builder.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), ParameterizedTypeName.get(ClassName.bestGuess(wrapperType.toString()), responseType)));
			builder.addStatement("$T<$T> wrapper = new $T<>()", wrapperType, responseType, wrapperType);
			this.addWrapperContent(builder);
			builder.addStatement("return new $T<>(wrapper, $T.OK)", ResponseEntity.class, HttpStatus.class);
		} else {
			this.addBasicReturn(builder);
		}
		return builder;
	}

	protected void addWrapperContent(MethodSpec.Builder builder) {
		builder.addStatement("wrapper.setContent(" + responseVariableName + ")");
	}

	protected void addBasicReturn(MethodSpec.Builder builder) {
		builder.addStatement("return new $T<>(" + responseVariableName + ", $T.OK)", ResponseEntity.class, HttpStatus.class);
	}
}
