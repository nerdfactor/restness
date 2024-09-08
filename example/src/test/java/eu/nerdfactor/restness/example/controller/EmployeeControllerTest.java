package eu.nerdfactor.restness.example.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.nerdfactor.restness.example.customer.CustomerDao;
import eu.nerdfactor.restness.example.entity.Employee;
import eu.nerdfactor.restness.example.repository.EmployeeRepository;
import eu.nerdfactor.restness.data.DataPage;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

	private static final String API_PATH = "/api/employee";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper jsonMapper;

	@Autowired
	EmployeeRepository repository;

	@Test
	@WithMockUser(roles = "READ_EMPLOYEE")
	void shouldLoadExistingEmployee() throws Exception {
		Employee employee = new Employee();
		employee.setPerNo(12345);
		employee.setName("Jonathan Pym");
		employee = this.repository.save(employee);
		mockMvc.perform(get(API_PATH + "/" + employee.getPerNo()))
				.andExpect(status().isOk())
				.andExpect(content().json(jsonMapper.writeValueAsString(employee)));
	}

	@Test
	@WithMockUser(roles = "READ_EMPLOYEE")
	void shouldReturn404ErrorLoadingNotExistingEmployee() throws Exception {
		mockMvc.perform(get(API_PATH + "/123456"))
				.andExpect(result -> assertInstanceOf(EntityNotFoundException.class, result.getResolvedException()))
				.andExpect(status().is(HttpStatus.NOT_FOUND.value()));
	}

	@Test
	@WithMockUser(roles = "CREATE_EMPLOYEE")
	void shouldCreateNewEmployee() throws Exception {
		Employee employee = new Employee();
		employee.setPerNo(34567);
		employee.setName("Janet van Dyne");
		mockMvc.perform(post(API_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.jsonMapper.writeValueAsString(employee))
		).andExpect(status().isOk());
		Employee employeeCheck = this.repository.findById(employee.getPerNo()).orElse(null);
		assertNotNull(employeeCheck);
		assertEquals(employee.getName(), employeeCheck.getName());
	}

	@Test
	@WithMockUser(roles = "UPDATE_EMPLOYEE")
	void shouldSetExistingEmployee() throws Exception {
		Employee employee1 = new Employee();
		employee1.setPerNo(45678);
		employee1.setName("Tony Stark");
		employee1.setEmail("tony@example.com");
		employee1 = this.repository.save(employee1);
		Employee employee2 = new Employee();
		employee2.setPerNo(employee1.getPerNo());
		employee2.setName("Bruce Banner");
		employee1.setEmail("hulk@example.com");
		mockMvc.perform(put(API_PATH + "/" + employee1.getPerNo())
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.jsonMapper.writeValueAsString(employee2))
		).andExpect(status().isOk());
		Employee employeeCheck = this.repository.findById(employee1.getPerNo()).orElse(null);
		assertNotNull(employeeCheck);
		assertEquals(employee2.getName(), employeeCheck.getName());
		assertEquals(employee2.getEmail(), employeeCheck.getEmail());
	}

	@Test
	@WithMockUser(roles = "UPDATE_EMPLOYEE")
	void shouldUpdateExistingEmployee() throws Exception {
		Employee employee1 = new Employee();
		employee1.setPerNo(56789);
		employee1.setName("Steven Rogers");
		employee1.setEmail("captain@example.com");
		employee1 = this.repository.save(employee1);
		Employee employee2 = new Employee();
		employee2.setPerNo(employee1.getPerNo());
		employee2.setName("Carol Danvers");
		mockMvc.perform(patch(API_PATH + "/" + employee1.getPerNo())
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.jsonMapper.writeValueAsString(employee2))
		).andExpect(status().isOk());
		Employee employeeCheck = this.repository.findById(employee1.getPerNo()).orElse(null);
		assertNotNull(employeeCheck);
		assertEquals(employee2.getName(), employeeCheck.getName());
		assertEquals(employee1.getEmail(), employeeCheck.getEmail());
	}

	@Test
	@WithMockUser(roles = "DELETE_EMPLOYEE")
	void shouldDeleteExistingEmployee() throws Exception {
		Employee employee = new Employee();
		employee.setPerNo(67890);
		employee.setName("Clinton Barton");
		employee = this.repository.save(employee);
		mockMvc.perform(delete(API_PATH + "/" + employee.getPerNo()))
				.andExpect(status().is(HttpStatus.NO_CONTENT.value()));
		Employee employeeCheck = this.repository.findById(employee.getPerNo()).orElse(null);
		assertNull(employeeCheck);
	}

	@Test
	@WithMockUser(roles = "READ_EMPLOYEE")
	void shouldLoadExistingEmployeeList() throws Exception {
		long count = StreamSupport.stream(this.repository.findAll().spliterator(), false).count();
		Employee employee1 = new Employee();
		employee1.setPerNo(78901);
		employee1.setName("T'Challa");
		employee1 = this.repository.save(employee1);
		Employee employee2 = new Employee();
		employee2.setPerNo(89012);
		employee2.setName("Victor Shade");
		employee2 = this.repository.save(employee2);
		Employee employee3 = new Employee();
		employee3.setPerNo(90123);
		employee3.setName("America Chavez");        // alphabetically first
		employee3 = this.repository.save(employee3);
		String json = mockMvc.perform(get(API_PATH))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		List<Employee> response = this.jsonMapper.readValue(json, new TypeReference<>() {
		});
		assertEquals(3 + count, response.size());
		response.sort(Comparator.comparing(Employee::getName));
		assertEquals(employee3.getPerNo(), response.get(0).getPerNo());
	}

	/**
	 * Should load the list of existing employees and ignore the paging
	 * request because {@link EmployeeRepository} does not provide sorting and
	 * paging.
	 */
	@Test
	@WithMockUser(roles = "READ_EMPLOYEE")
	void shouldLoadExistingEmployeeListAndIgnorePaging() throws Exception {
		long count = StreamSupport.stream(this.repository.findAll().spliterator(), false).count();
		Employee employee1 = new Employee();
		employee1.setPerNo(98765);
		employee1.setName("Charles Xavier");
		employee1 = this.repository.save(employee1);
		Employee employee2 = new Employee();
		employee2.setPerNo(87654);
		employee2.setName("Scott Summers");
		employee2 = this.repository.save(employee2);
		Employee employee3 = new Employee();
		employee3.setPerNo(76543);
		employee3.setName("Robert Drake");
		employee3 = this.repository.save(employee3);

		String response = mockMvc.perform(get(API_PATH + "/search?page=1&size=1"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		DataPage<CustomerDao> responses = this.jsonMapper.readValue(response, new TypeReference<>() {
		});

		assertNotNull(responses);
		assertEquals(count + 3, responses.getTotalElements());
		assertEquals(count + 3, responses.getContent().size());
	}
}
