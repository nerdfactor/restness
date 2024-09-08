package eu.nerdfactor.restness.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.nerdfactor.restness.example.dto.OrderDto;
import eu.nerdfactor.restness.example.entity.OrderModel;
import eu.nerdfactor.restness.example.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

	private static final String API_PATH = "/api/orders";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper jsonMapper;

	@Autowired
	OrderRepository repository;

	@Test
	@WithMockUser(roles = {"READ_ORDER"})
	void loadOrder() throws Exception {
		LocalDateTime now = LocalDateTime.now();
		OrderModel order = new OrderModel();
		order.setOrderedAt(now);
		order = this.repository.save(order);
		String json = mockMvc.perform(get(API_PATH + "/" + order.getId()))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		OrderDto response = this.jsonMapper.readValue(json, OrderDto.class);
		assertEquals(order.getId(), response.getId());
		// milliseconds will be different after loading from DB, therefor only compare day
		assertEquals(now.getDayOfMonth(), response.getOrderedAt().getDayOfMonth());
	}
}
