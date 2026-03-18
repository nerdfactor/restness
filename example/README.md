# RESTness Example

This module provides a sample Spring Boot application demonstrating the usage of the RESTness library.

It showcases:

* Defining entities with different naming Patterns (e.g., `OrderModel`, `ProductEntity`, `CustomerDao`, `Employee`).
* Creating corresponding DTOs or usage without DTOs (e.g., `OrderDto`).
* Setting up Spring Data repositories with or without the use of RESTness Data (e.g., `OrderRepository`, `ProductRepository`, `CustomerRepository`) and services (`OrderService`).
* Configuring RESTness using annotations (`@RestnessConfiguration`, `@RestnessController`, `@RestnessSecurity`) in `RestnessConfig`.
* Implementing custom `DataAccessor`, `DataMapper`, and `DataMerger` beans.
* Enabling OpenAPI documentation via `openApi = true` in `@RestnessConfiguration` and integrating `springdoc-openapi-starter-webmvc-ui` for Swagger UI.
* Using custom SPI extensions from the `example-extensions` module (custom method builder and injector).

To run the example:

1. Build the parent project (`mvn clean install` from the `restness/main` directory).
2. Run this example module as a standard Spring Boot application.
3. Access the generated endpoints (e.g., `/api/orders`, `/api/products`). Check the `RestnessConfig` file for specific paths.
4. Access the Swagger UI at `/swagger-ui.html` to explore the generated API documentation.
