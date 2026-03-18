# RESTness Example Extensions

This module demonstrates how to extend the RESTness code generation pipeline using the Service Provider Interface (SPI) extension points defined in the `core` module.

Extensions are discovered automatically via Java's `ServiceLoader` during annotation processing — no changes to the core library required.

## Custom Method Builder

`CountEndpointBuilder` / `CountEndpointBuilderProvider` adds a `count` endpoint (e.g., `GET /api/products/count`) to generated controllers that returns the total number of entities as a `Long` value.

* Implements `RestnessMethodBuilderProvider` to act as a factory for the builder.
* Registered via `META-INF/services/eu.nerdfactor.restness.code.methodbuilder.RestnessMethodBuilderProvider`.

## Custom Injector

`RateLimitedInjector` injects a `@RateLimited` annotation on all write methods (POST, PUT, PATCH) with a configurable requests-per-minute limit.

* Implements `ContextualInjectable` to selectively target methods by HTTP verb.
* Registered via `META-INF/services/eu.nerdfactor.restness.code.injector.ContextualInjectable`.

## Usage

Add this module as a dependency alongside `restness-core` in your project. The SPI registrations ensure the extensions are picked up automatically during the build.
