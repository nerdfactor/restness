# RESTness Processing

This module contains the Java Annotation Processor (`RestnessAnnotationProcessor`) responsible for gathering the configuration and generation of the REST controller at compile time.

Key functions:

* Registers itself to process RESTness annotations (`@RestnessConfiguration`, `@RestnessController`, `@RestnessSecurity`).
* Scans the annotated elements during the Maven build process.
* Builds internal configuration models based on the extracted annotation data.
* Supports the `openApi` parameter in `@RestnessConfiguration` to enable OpenAPI annotation generation on endpoints.
* Detects `@Id` annotations on entity fields for automatic accessor/modifier name derivation (e.g., a field `perNo` yields `getPerNo`/`setPerNo`).
* Imports additional controller configurations from JSON or YAML files and merges them with annotation-based configurations.
* Resolves generators dynamically via `RestnessGeneratorFactory` using Java's `ServiceLoader`.
* Triggers the code generation process by invoking the resolved generator components.
* Writes the generated Java source files.
