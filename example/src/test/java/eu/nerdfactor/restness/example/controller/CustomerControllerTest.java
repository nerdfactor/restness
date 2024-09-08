package eu.nerdfactor.restness.example.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.nerdfactor.restness.example.customer.CustomerDao;
import eu.nerdfactor.restness.example.customer.CustomerRepository;
import eu.nerdfactor.restness.data.DataPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {

	private static final String API_PATH = "/api/customers";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper jsonMapper;

	@Autowired
	CustomerRepository repository;

	@Test
	@WithMockUser(roles = "READ_CUSTOMER")
	void shouldLoadExistingCustomer() throws Exception {
		CustomerDao customer = new CustomerDao();
		customer.setEmail("peter@example.com");
		customer.setName("Peter Parker");
		customer = this.repository.save(customer);

		mockMvc.perform(get(API_PATH + "/" + customer.getEmail()))
				.andExpect(status().isOk())
				.andExpect(content().json(jsonMapper.writeValueAsString(customer)));
	}

	/**
	 * Should load the list of existing customers, filter them by name
	 * and restrict the result to the first page.
	 *
	 * @see <a href="https://github.com/turkraft/spring-filter">https://github.com/turkraft/spring-filter</a>
	 */
	@Test
	@WithMockUser(roles = "READ_CUSTOMER")
	void shouldLoadExistingCustomerListPagedAndFilteredByName() throws Exception {
		CustomerDao customer1 = new CustomerDao();
		customer1.setEmail("pietro@example.com");
		customer1.setName("Pietro Maximoff");
		customer1 = this.repository.save(customer1);
		CustomerDao customer2 = new CustomerDao();
		customer2.setEmail("wanda@example.com");
		customer2.setName("Wanda Maximoff");
		customer2 = this.repository.save(customer2);
		CustomerDao customer3 = new CustomerDao();
		customer3.setEmail("natasha@example.com");
		customer3.setName("Natasha Romanoff");
		customer3 = this.repository.save(customer3);
		CustomerDao customer4 = new CustomerDao();
		customer4.setEmail("vance@example.com");
		customer4.setName("Vance Astrovik");
		customer4 = this.repository.save(customer4);

		String response = mockMvc.perform(get(API_PATH + "/search?page=1&size=1&filter=name~~'%maximoff'"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();

		DataPage<CustomerDao> responses = this.jsonMapper.readValue(response, new TypeReference<>() {
		});

		assertNotNull(responses);
		assertEquals(2, responses.getTotalElements());
		assertEquals(1, responses.getContent().size());
	}
}
