package eu.nerdfactor.restness.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.nerdfactor.restness.example.dto.ProductDto;
import eu.nerdfactor.restness.example.entity.ProductEntity;
import eu.nerdfactor.restness.example.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

	private static final String API_PATH = "/api/products";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper jsonMapper;

	@Autowired
	ProductRepository repository;

	/**
	 * Uses the controller directly and not through http in order to mock the
	 * user and permissions. Permissions are required to access methods in
	 * {@link ProductRepository}.
	 */
	@Autowired
	RestnessProductController controller;

	ModelMapper mapper = new ModelMapper();

	/**
	 * Should not be able to load the product because {@link ProductRepository}
	 * restricts access to data and the http request is not authenticating
	 * itself to get permissions.
	 */
	@Test
	@WithMockUser(roles = {"UPDATE_PRODUCT"})
	void shouldReturn403ErrorLoadingProductWithoutPermission() throws Exception {
		final ProductEntity product = new ProductEntity();
		product.setName("Black Vortex");
		this.repository.save(product);
		mockMvc.perform(get(API_PATH + "/1"))
				.andExpect(result -> assertInstanceOf(AccessDeniedException.class, result.getResolvedException()))
				.andExpect(status().is(HttpStatus.FORBIDDEN.value()));
	}

	@Test
	@WithMockUser(roles = {"UPDATE_PRODUCT", "READ_PRODUCT"})
	void shouldLoadExistingProduct() {
		ProductEntity product = new ProductEntity();
		product.setName("The Casket of Ancient Winters");
		product = this.repository.save(product);
		ResponseEntity<ProductDto> response = this.controller.get(product.getId());
		assertNotNull(response.getBody());
		assertEquals(product.getId(), response.getBody().getId());
	}

	@Test
	@WithMockUser(roles = {"UPDATE_PRODUCT", "READ_PRODUCT"})
	void shouldCreateNewProduct() {
		ProductEntity product = new ProductEntity();
		product.setName("The Crimson Gem of Cyttorak");
		HttpEntity<ProductDto> response = this.controller.create(this.mapper.map(product, ProductDto.class));
		assertNotNull(response.getBody());
		ProductEntity productCheck = this.repository.findById(response.getBody().getId()).orElse(null);
		assertNotNull(productCheck);
		assertEquals(product.getName(), productCheck.getName());
	}

	@Test
	@WithMockUser(roles = {"UPDATE_PRODUCT", "READ_PRODUCT", "DELETE_PRODUCT"})
	void shouldDeleteExistingProduct() {
		ProductEntity product = new ProductEntity();
		product.setName("The Darkhold");
		product = this.repository.save(product);
		this.controller.delete(product.getId());
		ProductEntity productCheck = this.repository.findById(product.getId()).orElse(null);
		assertNull(productCheck);
	}
}
