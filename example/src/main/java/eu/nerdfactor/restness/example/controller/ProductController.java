package eu.nerdfactor.restness.example.controller;

import eu.nerdfactor.restness.annotation.RestnessController;
import eu.nerdfactor.restness.example.dto.ProductDto;
import eu.nerdfactor.restness.example.entity.ProductEntity;

/**
 * Base controller for Products.<br> Uses RestnessController to configure a
 * generated controller based on the controller. Provides information about the
 * entity and id to use in the generated controller.
 */
@RestnessController(value = "/api/products", entity = ProductEntity.class, id = Integer.class, dto = ProductDto.class)
public class ProductController {
}
