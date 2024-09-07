package eu.nerdfactor.restness.code.injector;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * An injector that adds a return statement to a method.
 *
 * @author Daniel Klug
 */
public class ReturnStatementInjector implements Injectable<MethodSpec.Builder> {

	/**
	 * The class of a wrapper object.
	 */
	protected TypeName wrapperType;

	/**
	 * The class of the response object.
	 */
	protected TypeName responseType;

	/**
	 * The name of the response variable used in the method.
	 */
	protected String responseVariableName = "response";

	/**
	 * @param wrapper The class of a wrapper object
	 * @return The injector in a fluent api pattern.
	 */
	public ReturnStatementInjector withWrapper(TypeName wrapper) {
		this.wrapperType = wrapper;
		return this;
	}

	/**
	 * @param response The class of the response object
	 * @return The injector in a fluent api pattern.
	 */
	public ReturnStatementInjector withResponse(TypeName response) {
		this.responseType = response;
		return this;
	}

	/**
	 * @param variable The name of the response variable.
	 * @return The injector in a fluent api pattern.
	 */
	public ReturnStatementInjector withResponseVariable(String variable) {
		this.responseVariableName = variable;
		return this;
	}

	/**
	 * Inject into a {@link MethodSpec.Builder} and add a return statement.
	 *
	 * @param builder An existing builder object that will be used.
	 * @return The altered {@link MethodSpec.Builder}.
	 */
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

	/**
	 * Add a statement that wraps the response object inside the wrapper.
	 *
	 * @param builder An existing builder object that will be used.
	 */
	protected void addWrapperContent(MethodSpec.Builder builder) {
		builder.addStatement("wrapper.setContent(" + responseVariableName + ")");
	}

	/**
	 * Add a statement that returns the response object.
	 *
	 * @param builder An existing builder object that will be used.
	 */
	protected void addBasicReturn(MethodSpec.Builder builder) {
		builder.addStatement("return new $T<>(" + responseVariableName + ", $T.OK)", ResponseEntity.class, HttpStatus.class);
	}
}
