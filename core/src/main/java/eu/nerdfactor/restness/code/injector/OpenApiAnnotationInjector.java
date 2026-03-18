package eu.nerdfactor.restness.code.injector;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An injector that adds OpenAPI/Swagger annotations to a method.
 * Adds @Operation (summary, operationId) and @ApiResponses containing
 * a success @ApiResponse and optional error @ApiResponse entries.
 * <p>
 * All Swagger class references use {@code ClassName.get()} with string
 * package/class names, avoiding a compile dependency on swagger-annotations.
 * Consumer projects must have swagger-annotations (2.2.0+) on their classpath.
 *
 * @author Daniel Klug
 */
public class OpenApiAnnotationInjector implements Injectable<MethodSpec.Builder> {

	private static final ClassName OPERATION_CLASS =
			ClassName.get("io.swagger.v3.oas.annotations", "Operation");

	private static final ClassName API_RESPONSE_CLASS =
			ClassName.get("io.swagger.v3.oas.annotations.responses", "ApiResponse");

	private static final ClassName API_RESPONSES_CLASS =
			ClassName.get("io.swagger.v3.oas.annotations.responses", "ApiResponses");

	/**
	 * Whether OpenAPI annotation generation is enabled.
	 */
	protected boolean enabled = false;

	/**
	 * The summary text for the @Operation annotation.
	 */
	protected String operationSummary;

	/**
	 * The operationId for the @Operation annotation.
	 */
	protected String operationId;

	/**
	 * The HTTP status code for the success response.
	 */
	protected String responseCode = "200";

	/**
	 * The description for the success response.
	 */
	protected String responseDescription;

	/**
	 * Error responses keyed by HTTP status code with description values.
	 */
	protected Map<String, String> errorResponses = new LinkedHashMap<>();

	/**
	 * @param enabled Whether OpenAPI annotations should be generated.
	 * @return The injector in a fluent api pattern.
	 */
	public OpenApiAnnotationInjector withEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	/**
	 * @param summary The summary text for the @Operation annotation.
	 * @return The injector in a fluent api pattern.
	 */
	public OpenApiAnnotationInjector withOperationSummary(String summary) {
		this.operationSummary = summary;
		return this;
	}

	/**
	 * @param id The operationId for the @Operation annotation.
	 * @return The injector in a fluent api pattern.
	 */
	public OpenApiAnnotationInjector withOperationId(String id) {
		this.operationId = id;
		return this;
	}

	/**
	 * @param code The HTTP status code for the success response.
	 * @return The injector in a fluent api pattern.
	 */
	public OpenApiAnnotationInjector withResponseCode(String code) {
		this.responseCode = code;
		return this;
	}

	/**
	 * @param description The description for the success response.
	 * @return The injector in a fluent api pattern.
	 */
	public OpenApiAnnotationInjector withResponseDescription(String description) {
		this.responseDescription = description;
		return this;
	}

	/**
	 * Add an error response to the @ApiResponses annotation.
	 *
	 * @param code        The HTTP status code for the error response.
	 * @param description The description for the error response.
	 * @return The injector in a fluent api pattern.
	 */
	public OpenApiAnnotationInjector addErrorResponse(String code, String description) {
		this.errorResponses.put(code, description);
		return this;
	}

	/**
	 * Inject OpenAPI annotations into a {@link MethodSpec.Builder}.
	 * Adds @Operation and @ApiResponses annotations when enabled.
	 *
	 * @param builder An existing builder object that will be used.
	 * @return The altered {@link MethodSpec.Builder}.
	 */
	@Override
	public MethodSpec.Builder inject(MethodSpec.Builder builder) {
		if (!this.enabled) {
			return builder;
		}

		builder.addAnnotation(AnnotationSpec.builder(OPERATION_CLASS)
				.addMember("summary", "$S", this.operationSummary)
				.addMember("operationId", "$S", this.operationId)
				.build());

		AnnotationSpec.Builder apiResponsesBuilder = AnnotationSpec.builder(API_RESPONSES_CLASS);

		apiResponsesBuilder.addMember("value", "$L",
				AnnotationSpec.builder(API_RESPONSE_CLASS)
						.addMember("responseCode", "$S", this.responseCode)
						.addMember("description", "$S", this.responseDescription)
						.build());

		for (Map.Entry<String, String> error : this.errorResponses.entrySet()) {
			apiResponsesBuilder.addMember("value", "$L",
					AnnotationSpec.builder(API_RESPONSE_CLASS)
							.addMember("responseCode", "$S", error.getKey())
							.addMember("description", "$S", error.getValue())
							.build());
		}

		builder.addAnnotation(apiResponsesBuilder.build());
		return builder;
	}
}
