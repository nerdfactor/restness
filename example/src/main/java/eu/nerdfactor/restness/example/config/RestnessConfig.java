package eu.nerdfactor.restness.example.config;

import com.turkraft.springfilter.converter.FilterSpecificationConverterImpl;
import eu.nerdfactor.restness.annotation.RestnessConfiguration;
import eu.nerdfactor.restness.annotation.RestnessController;
import eu.nerdfactor.restness.annotation.RestnessSecurity;
import eu.nerdfactor.restness.data.*;
import eu.nerdfactor.restness.example.dto.OrderDto;
import eu.nerdfactor.restness.example.entity.Employee;
import eu.nerdfactor.restness.example.entity.OrderModel;
import eu.nerdfactor.restness.example.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import static eu.nerdfactor.restness.annotation.RestnessConfiguration.INDENT_SPACE;

/**
 * Configuration of GeneratedRest.<br> Uses {@link RestnessConfiguration}
 * annotation to configure GeneratedRest. Different default values, like naming
 * of generated controllers or file indentation can be set.
 * <br>
 * Uses {@link RestnessController} to configure a generated controller without
 * creating a separate class for it. This requires the full class name for that
 * generated controller in addition to the normal values (entity, id, dto) for
 * the configuration.
 * <br>
 * Uses {@link RestnessSecurity} to configure Spring security for the generated
 * controller. Requires the full class name to match to the generated
 * controller.
 */
@Component
@RequiredArgsConstructor
@RestnessConfiguration(indentation = INDENT_SPACE, classNamePattern = "Restness{NAME_NORMALIZED}Controller", log = true)
@RestnessController(className = "eu.nerdfactor.restness.example.controller.OrderController", value = "/api/orders", entity = OrderModel.class, id = Integer.class, dto = OrderDto.class)
@RestnessSecurity(className = "eu.nerdfactor.restness.example.controller.OrderController")
public class RestnessConfig {


	private final EmployeeRepository employeeRepository;

	/**
	 * Provides a bean of {@link DataAccessor} for {@link Employee Employees}
	 * that implements a custom way to access those entities.
	 *
	 * @return A new DataAccessor
	 */
	@Bean
	public DataAccessor<Employee, Integer> getEmployeeDataAccessor() {
		return new DataAccessService<Employee, Integer>() {
			@Override
			public CrudRepository<Employee, Integer> getRepository() {
				return employeeRepository;
			}
		};
	}

	/**
	 * Provides a bean of {@link DataMapper} to map between entities and dto.
	 * This inline implementation passes the call to map() on to a
	 * {@link ModelMapper} object. This way different mapping libraries can be
	 * used.
	 *
	 * @return A new DataMapper
	 */
	@Bean
	public DataMapper getDataMapper() {
		final ModelMapper mapper = new ModelMapper();
		return new DataMapper() {
			@Override
			public <T> T map(Object o, Class<T> cls) {
				return mapper.map(o, cls);
			}
		};
	}

	/**
	 * Provides a bean of {@link DataMerger} to update existing entities with
	 * new values. This inline implementation passes the call to merge() on to a
	 * {@link ModelMapper} object. This way different mapping libraries can be
	 * used.
	 *
	 * @return A new DataMerger
	 */
	@Bean
	public DataMerger getDataMerger() {
		final ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);
		return new DataMerger() {
			@Override
			public <T> T merge(T t, T t1) {
				mapper.map(t1, t);
				return t;
			}
		};
	}

	/**
	 * Provides a bean of {@link DataSpecificationBuilder} to build
	 * {@link Specification Specifications} from a filter string.
	 *
	 * @param converter A turkraft {@link FilterSpecificationConverterImpl }
	 *                  that will convert the filter string.
	 * @return A new DataSpecificationBuilder
	 */
	@Bean
	public DataSpecificationBuilder getSpecificationBuilder(@Autowired final FilterSpecificationConverterImpl converter) {
		return new DataSpecificationBuilder() {
			@Override
			public <T> Specification<T> build(String filter, Class<T> cls) {
				if (filter == null || filter.isBlank()) {
					return Specification.where(null);
				} else {
					return converter.convert(filter);
				}
			}
		};
	}

}
