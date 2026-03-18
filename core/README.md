# RESTness Core

This module provides the core building blocks and utilities for generating REST controller code. It leverages the [JavaPoet](https://github.com/square/javapoet) library to construct Java source files programmatically.

## Key Components

* **Method Builders** (e.g., `ReadEntityMethodBuilder`, `RelationshipMethodBuilder`): Classes responsible for generating specific methods within the controllers (like CRUD operations and relationship management). All concrete builders extend `MultiStepMethodBuilder`, which provides common configuration and injector registry support.
* **Injectors** (e.g., `ReturnStatementInjector`, `AuthenticationInjector`): Classes that inject specific code snippets or logic (like return statements with wrappers or security checks) into generated methods.
* **Utilities** (e.g., `RestnessUtil`, `WordInflector`): Helper classes for common tasks like name normalization and string manipulation during code generation.

## Extensibility via SPI

The module provides two Service Provider Interfaces (SPI) that allow extending the code generation pipeline without modifying core code. Extensions are discovered automatically via Java's `ServiceLoader`.

### Custom Injectors

Implement `ContextualInjectable` to inject cross-cutting concerns (annotations, validation, logging, etc.) into generated methods. Each injector receives a `MethodContext` containing metadata about the method being generated (method name, HTTP verb, entity type, relation info) and can decide whether to apply based on that context.

Register via `META-INF/services/eu.nerdfactor.restness.code.injector.ContextualInjectable`.

### Custom Method Builders

Implement `RestnessMethodBuilderProvider` to add entirely new endpoints beyond the standard CRUD and relationship methods. The provider acts as a factory, creating configured builder instances that generate additional methods on the controller.

Register via `META-INF/services/eu.nerdfactor.restness.code.methodbuilder.RestnessMethodBuilderProvider`.

### Registries

* **`MethodInjectorRegistry`**: Discovers, sorts (by `getOrder()`), and dispatches `ContextualInjectable` implementations to matching methods.
* **`MethodBuilderRegistry`**: Discovers and instantiates `RestnessMethodBuilderProvider` implementations, running custom builders after the built-in ones.

## Built-in Injectors

* **`AuthenticationInjector`**: Adds Spring Security `@Secured` annotations for entity-level access control.
* **`RelationAuthenticationInjector`**: Extends `AuthenticationInjector` for relationship-level security, supporting composite security expressions that combine related and base entity roles.
* **`OpenApiAnnotationInjector`**: Adds OpenAPI 3.0 annotations (`@Operation`, `@ApiResponses`) to generated methods when OpenAPI support is enabled.
* **`ReturnStatementInjector`**: Generates return statements with optional response wrapping.
* **`NoContentStatementInjector`**: Generates return statements with HTTP 204 No Content status.
