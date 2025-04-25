package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.*;
import eu.nerdfactor.restness.code.injector.AuthenticationInjector;
import eu.nerdfactor.restness.code.injector.ReturnStatementInjector;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.config.SecurityConfiguration;
import eu.nerdfactor.restness.data.DataPage;
import eu.nerdfactor.restness.util.RestnessUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating the method that searches for entities based on a filter
 * and returns a paginated result in a REST controller.
 * This builder is responsible for generating the {@code searchAll(filter, pageable)} method,
 * typically annotated with {@code @GetMapping("/search")}. It handles checking for
 * existing methods, injecting authentication logic, defining the method body for
 * building the specification, performing the search, handling DTO mapping,
 * creating the response page, and wrapping the response.
 *
 * @author Daniel Klug
 */
@Slf4j
@With
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchMethodBuilder extends MethodBuilder {

	/**
	 * Flag indicating if a request mapping for searching entities already exists.
	 */
	protected boolean requestExists;

	/**
	 * The base request path for the controller (e.g., "/entities").
	 * The search path will be appended to this (e.g., "/entities/search").
	 */
	private String basePath;

	/**
	 * The {@link TypeName} of the response object (DTO or Entity) contained in the page.
	 */
	private TypeName responseBodyType;

	/**
	 * The {@link TypeName} of the entity class being searched.
	 */
	private TypeName entityType;

	/**
	 * The {@link SecurityConfiguration} for the controller method.
	 */
	private SecurityConfiguration securityConfig;

	/**
	 * Flag indicating if Data Transfer Objects (DTOs) are used for the response.
	 */
	private boolean isUsingDto;

	/**
	 * The {@link TypeName} of the class used to wrap the response page, if any.
	 */
	private TypeName responseWrapperType;

	/**
	 * Create a new instance of {@link SearchMethodBuilder}.
	 *
	 * @return A new {@link SearchMethodBuilder}.
	 */
	public static SearchMethodBuilder create() {
		return new SearchMethodBuilder();
	}

	/**
	 * Configures the builder with the provided {@link ControllerConfiguration}.
	 * It extracts necessary information like request paths, types, security settings,
	 * and checks for existing GET mappings for the search endpoint.
	 *
	 * @param configuration The {@link ControllerConfiguration} for the controller.
	 * @return A new configured instance of {@link SearchMethodBuilder}.
	 */
	public SearchMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		return this.withRequestExists(RestnessUtil.hasExistingRequest(configuration.getExistingRequestMappings(), configuration.getRequestBasePath() + "/search", RequestMethod.GET))
				.withBasePath(configuration.getRequestBasePath())
				.withResponseBodyType(configuration.getResponseType())
				.withEntityType(configuration.getEntityClassName())
				.withSecurityConfig(configuration.getSecurityConfiguration())
				.withUsingDto(configuration.isUsingDto())
				.withResponseWrapperType(configuration.getResponseWrapperClassName());
	}


	/**
	 * Builds the search entities method and adds it to the provided {@link TypeSpec.Builder}.
	 * If a method with the same signature already exists, it skips the generation.
	 * Otherwise, it creates the method declaration, injects authentication, adds the
	 * method body (including specification building, data retrieval, mapping, and page creation),
	 * and injects the return statement logic.
	 *
	 * @param builder The {@link TypeSpec.Builder} for the controller class.
	 * @return The updated {@link TypeSpec.Builder} with the new method added (if applicable).
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		if (this.requestExists) {
			return builder;
		}
		log.info("addSearchAllEntitiesMethod");
		ParameterizedTypeName responsePageType = ParameterizedTypeName.get(ClassName.get(Page.class), this.responseBodyType);
		MethodSpec.Builder method = MethodSpec
				.methodBuilder("searchAll")
				.addAnnotation(AnnotationSpec.builder(GetMapping.class).addMember("value", "$S", this.basePath + "/search").build())
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(ResponseEntity.class), responsePageType))
				.addParameter(ParameterSpec.builder(String.class, "filter")
						.addAnnotation(AnnotationSpec.builder(RequestParam.class)
								.addMember("required", "false").
								build()
						)
						.build()
				)
				.addParameter(ParameterSpec.builder(Pageable.class, "pageable")
						.addAnnotation(AnnotationSpec.builder(PageableDefault.class).addMember("size", "20").build()).
						build()
				);

		new AuthenticationInjector()
				.withMethod("READ")
				.withEntityClassName(this.entityType)
				.withSecurityConfig(this.securityConfig)
				.inject(method);

		this.addMethodBody(method, this.entityType, this.responseBodyType, this.isUsingDto);

		new ReturnStatementInjector()
				.withWrapper(this.responseWrapperType)
				.withResponse(responsePageType) // Use the Page<ResponseType> for the injector
				.withResponseVariable("responsePage")
				.inject(method);
		builder.addMethod(method.build());
		return builder;
	}

	/**
	 * Adds the method body for searching entities. This includes:
	 * 1. Building a {@link Specification} from the filter string.
	 * 2. Calling the data accessor to search for data using the specification and pageable.
	 * 3. Iterating through the results and mapping entities to response objects (if DTOs are used).
	 * 4. Creating a new {@link Page} containing the response objects and original pagination info.
	 *
	 * @param method          The {@link MethodSpec.Builder} to add the body to.
	 * @param entityClassName The {@link TypeName} of the entity.
	 * @param responseType    The {@link TypeName} of the response object (DTO or entity).
	 * @param usingDto        Flag indicating if DTOs are used.
	 */
	protected void addMethodBody(MethodSpec.Builder method, TypeName entityClassName, TypeName responseType, boolean usingDto) {
		method.addStatement("$T<$T> spec = this.specificationBuilder.build(filter, $T.class)", Specification.class, entityClassName, entityClassName);
		method.addStatement("$T<$T> responseList = new $T<>()", List.class, responseType, ArrayList.class);
		method.addStatement("$T page = this.dataAccessor.searchData(spec, pageable)", ParameterizedTypeName.get(ClassName.get(Page.class), entityClassName));
		method.beginControlFlow("for($T entity : page.getContent())", entityClassName);
		if (usingDto) {
			method.addStatement("$T response = this.dataMapper.map(entity, $T.class)", responseType, responseType);
		} else {
			method.addStatement("$T response = entity", responseType);
		}
		method.addStatement("responseList.add(response)");
		method.endControlFlow();
		method.addStatement("$T<$T> responsePage = new $T<>(responseList, page.getPageable(), page.getTotalElements())", Page.class, responseType, DataPage.class);
	}
}