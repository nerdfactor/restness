package eu.nerdfactor.restness.code.injector;

import com.squareup.javapoet.TypeName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Immutable value object carrying method generation context. Custom
 * {@link ContextualInjectable} implementations use this to decide whether
 * they should inject into a particular generated method.
 *
 * @author Daniel Klug
 */
@Getter
@Builder(setterPrefix = "with")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MethodContext {

	/**
	 * The name of the generated method (e.g. "create", "read", "getCustomerOrders").
	 */
	private final String methodName;

	/**
	 * The HTTP method of the generated endpoint (e.g. "GET", "POST", "PUT", "PATCH", "DELETE").
	 */
	private final String httpMethod;

	/**
	 * The {@link TypeName} of the main entity managed by the controller.
	 */
	private final TypeName entityType;

	/**
	 * The {@link TypeName} of the related entity, if this is a relationship method.
	 * Null for non-relationship methods.
	 */
	private final TypeName relatedEntityType;

	/**
	 * The name of the relation property, if this is a relationship method.
	 * Null for non-relationship methods.
	 */
	private final String relationName;
}
