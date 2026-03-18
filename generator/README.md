# RESTness Generator

This module contains the primary logic for generating the REST controller source code based on the configurations.

Key responsibilities include:

* Orchestrating the code generation process using builders and injectors from the `core` module.
* Handling the export and import of RESTness configurations as JSON or YAML (via `ConfigMapper`, `JsonConfigExporter`, `YamlConfigExporter`).
* Translating the abstract configuration models into concrete Java code using JavaPoet.

## Generator Discovery

`RestnessGeneratorFactory` uses Java's `ServiceLoader` to discover `RestnessGenerator` implementations at runtime. Generators can be resolved by short name (e.g., `"java"`, `"json"`, `"yaml"`) or by fully qualified class name, with a fallback to `JavaClassGenerator` if no match is found.

All built-in generators (`JavaClassGenerator`, `JsonConfigExporter`, `YamlConfigExporter`) are registered via `@AutoService` for automatic SPI discovery.

## Serialization

The `ParameterizedTypeNameDeserializer` supports deserialization of nested generic types (e.g., `Map<String, List<String>>`) when importing configurations from JSON or YAML files.
