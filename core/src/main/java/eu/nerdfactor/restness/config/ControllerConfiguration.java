package eu.nerdfactor.restness.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.restness.data.DataAccessor;
import eu.nerdfactor.restness.data.DataMapper;
import eu.nerdfactor.restness.data.DataMerger;
import eu.nerdfactor.restness.data.DataWrapper;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for RESTness controller generation.
 *
 * @author Daniel Klug
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class ControllerConfiguration {

	/**
	 * The {@link ClassName} of the controller. This includes the full namespace
	 * and the name of the class.
	 *
	 * <li>{@code com.example.controller.ProductRestController}</li>
	 * <li>{@code com.example.controller.UserController}</li>
	 * <li>{@code com.example.controller.GeneratedOrderController}</li>
	 */
	protected ClassName controllerClassName;

	/**
	 * The base path for requests of the controller. It will be added in the
	 * beginning of the RequestMapping annotations for actions in the
	 * controller.
	 *
	 * <li>{@code /products}</li>
	 * <li>{@code /api/users}</li>
	 * <li>{@code /backoffice/order-management}</li>
	 */
	protected String requestBasePath;

	/**
	 * The {@link TypeName} for the entity managed by the controller. This
	 * includes the full namespace and the name of the class.
	 *
	 * <li>{@code com.example.entity.Product}</li>
	 * <li>{@code com.example.entity.UserEntity}</li>
	 * <li>{@code com.example.model.OrderModel}</li>
	 */
	protected TypeName entityClassName;

	/**
	 * The {@link TypeName} for the entities id field. This includes the full
	 * namespace and the name of the class. For primitive id types like int or
	 * long the Java object should be used. This is necessary due to the use of
	 * generics in Spring JPAs repositories (i.e. CrudRepository<T, ID>).
	 *
	 * <li>{@code java.lang.Integer}</li>
	 * <li>{@code java.lang.String}</li>
	 * <li>{@code java.util.UUID}</li>
	 */
	protected TypeName idClassName;

	/**
	 * The method name to access the id of the entity. This will be used in the
	 * controller to get the id of entities.
	 *
	 * <li>{@code getId}</li>
	 * <li>{@code getIdentifier}</li>
	 * <li>{@code getOrderNumber}</li>
	 */
	protected String idAccessorMethodName = "getId";

	/**
	 * The {@link TypeName} of the object that will be contained in the requests
	 * handled by the controller. This includes the full namespace and the name
	 * of the class. For example, the request object would be sent within a POST
	 * request to an update endpoint containing the new data for the entity.
	 * <p>
	 * The request object can be the same type as the entity (just keep the
	 * default value) or some different type if the controller should use data
	 * transfer objects.
	 *
	 * <li>{@code com.example.entity.Product}</li>
	 * <li>{@code com.example.dto.UserDto}</li>
	 * <li>{@code com.example.form.OrderUpdateForm}</li>
	 */
	protected TypeName requestObjectClassName = TypeName.OBJECT;

	/**
	 * The {@link TypeName} of the object that will returned by the
	 * controller.This includes the full namespace and the name of the class.
	 * For example, the response object would be sent as answer to a GET request
	 * for one specific entity.
	 * <p>
	 * The response object can be the same type as the entity (just keep the
	 * default value) or some different type if the controller should use data
	 * transfer objects.
	 *
	 * <li>{@code com.example.entity.Product}</li>
	 * <li>{@code com.example.dto.UserDto}</li>
	 * <li>{@code com.example.viewmodel.OrderViewModel}</li>
	 */
	protected TypeName responseObjectClassName = TypeName.OBJECT;

	/**
	 * The {@link TypeName} of a set of multiple objects that will returned by
	 * the controller.This includes the full namespace and the name of the
	 * wrapping and the contained class. For example, the response object would
	 * be sent as answer to a GET request for multiple entities.
	 * <p>
	 * The response object can be a {@link List} of the same type as the
	 * singleResponseObjectClassName (just keep it null) or some different type
	 * if the controller should use some specific set object.
	 *
	 * <li>{@code java.util.List<com.example.entity.Product>}</li>
	 * <li>{@code java.util.Set<com.example.dto.UserDto>}</li>
	 * <li>{@code
	 * com.example.ViewModelList<com.example.viewmodel.OrderViewModel>}</li>
	 */
	protected TypeName responseListClassName = TypeName.OBJECT;

	/**
	 * The {@link TypeName} of a class used as wrapper around the data returned
	 * by the controller. This includes the full namespace and the name of the
	 * class.
	 * <p>
	 * The response wrapper can be any class that implements the
	 * {@link DataWrapper} interface or contains methods with the same
	 * signature.
	 *
	 * <li>{@code com.example.api.DataResponse}</li>
	 * <li>{@code com.example.data.ResponseContainer}</li>
	 * <li>{@code eu.nerdfactor.restness.data.DataWrapper}</li>
	 */
	protected TypeName responseWrapperClassName;

	/**
	 * The {@link ParameterizedTypeName} of the service used to access entities.
	 * This includes the full namespace and name of the wrapping and the
	 * contained class.
	 * <p>
	 * The data accessor can be any class that implements the
	 * {@link DataAccessor} interface or contains methods with the same
	 * signature.
	 *
	 * <li>{@code
	 * com.example.service.ProductService<com.example.entity.Product>}</li>
	 * <li>{@code
	 * com.example.repo.UserRepository<com.example.entity.UserEntity,
	 * java.lang.Integer>}</li>
	 * <li>{@code com.example.data.OrderDao<com.example.model.OrderModel>}</li>
	 */
	protected ParameterizedTypeName dataAccessorClassName;

	/**
	 * The {@link TypeName} of the data merger that will be used to merge
	 * entities. This includes the full namespace and * the name of the class.
	 * <p>
	 * The data merger can be any class that implements the {@link DataMerger}
	 * interface or contains methods with the same signature.
	 *
	 * <li>{@code com.example.service.ProductUpdateService}</li>
	 * <li>{@code com.example.util.EntityUpdater}</li>
	 * <li>{@code eu.nerdfactor.restness.data.DataMerger}</li>
	 */
	protected TypeName dataMergerClassName;

	/**
	 * The {@link TypeName} of the data mapper that will be used to map between
	 * entities and data transfer objects. This includes the full namespace and
	 * the name of the class.
	 * <p>
	 * The data mapper will be only be used if the controller uses data transfer
	 * objects.
	 * <p>
	 * The data mapper can be any class that implements the {@link DataMapper}
	 * interface or contains methods with the same signature. A possible
	 * approach could be to provide an anonymous mapper in a Spring Bean.
	 *
	 * <li>{@code com.example.service.ProductMappingService}</li>
	 * <li>{@code com.example.util.DtoMapping}</li>
	 * <li>{@code eu.nerdfactor.restness.data.DataMapper}</li>
	 */
	protected TypeName dataMapperClassName;

	/**
	 * A {@link List} of existing RequestMappings that will be skipped during
	 * the generation of the controller.
	 */
	protected List<String> existingRequestMappings;

	/**
	 * A {@link SecurityConfiguration} used during generation of the
	 * controller.
	 */
	@Setter
	@JsonIgnore
	protected SecurityConfiguration securityConfiguration;

	/**
	 * A {@link Map} of {@link RelationConfiguration}s used during generation of
	 * the controller.
	 */
	@JsonIgnore
	protected Map<String, RelationConfiguration> relationConfigurations = new HashMap<>();

	/**
	 * Check if there already exists a RequestMapping for the combination of
	 * request method and path.
	 *
	 * @param method The method used for the request (i.e. GET or POST).
	 * @param path   The path used for this request (i.e. /api/users).
	 * @return True if there already exists a matching RequestMapping.
	 */
	@JsonIgnore
	public boolean hasExistingRequest(RequestMethod method, String path) {
		return this.hasExistingRequest(method.name().toUpperCase(), path);
	}

	/**
	 * Check if there already exists a RequestMapping for the combination of
	 * request method and path.
	 *
	 * @param method The method used for the request (i.e. GET or POST).
	 * @param path   The path used for this request (i.e. /api/users).
	 * @return True if there already exists a matching RequestMapping.
	 */
	@JsonIgnore
	public boolean hasExistingRequest(String method, String path) {
		return this.existingRequestMappings.contains(method.toUpperCase() + path.toLowerCase());
	}

	/**
	 * Get the {@link TypeName} of the single response object.
	 *
	 * @return The {@link TypeName} of the response object.
	 */
	@JsonIgnore
	public TypeName getResponseType() {
		return this.getSingleResponseType();
	}

	/**
	 * Get the Type of the single response object.
	 *
	 * @return The Type of the response object.
	 */
	@Deprecated
	@JsonIgnore
	public TypeName getSingleResponseType() {
		return this.isUsingDto() ? this.responseObjectClassName : this.entityClassName;
	}

	/**
	 * Get the {@link TypeName} of the list response object.
	 *
	 * @return The {@link TypeName} of the list response object.
	 */
	@JsonIgnore
	public TypeName getListResponseType() {
		return this.isUsingDto() ? this.responseListClassName : this.entityClassName;
	}

	/**
	 * Get the {@link TypeName} of the request object.
	 *
	 * @return The {@link TypeName} of the request object.
	 */
	@JsonIgnore
	public TypeName getRequestType() {
		return this.isUsingDto() ? this.responseObjectClassName : this.entityClassName;
	}

	/**
	 * Check if the controller uses relations.
	 *
	 * @return True if the controller uses relations.
	 */
	@JsonIgnore
	public boolean isUsingRelations() {
		return this.relationConfigurations != null && !this.relationConfigurations.isEmpty();
	}

	/**
	 * Check if the controller uses data transfer objects.
	 *
	 * @return True if the controller uses data transfer objects.
	 */
	@JsonIgnore
	public boolean isUsingDto() {
		return this.responseObjectClassName != null && !this.responseObjectClassName.equals(TypeName.OBJECT);
	}

	/**
	 * Creates a builder for controller configuration from annotations.
	 *
	 * @return A new {@link ControllerConfigurationFromAnnotationBuilder}.
	 */
	public static @NotNull ControllerConfigurationFromAnnotationBuilder annotationBuilder() {
		return new ControllerConfigurationFromAnnotationBuilder();
	}
}
