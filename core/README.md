# RESTness Core

This module provides the core building blocks and utilities for generating REST controller code. It leverages the [JavaPoet](https://github.com/square/javapoet) library to construct Java source files programmatically.

Key components include:

* **Method Builders** (e.g., `ReadEntityMethodBuilder`, `RelationshipMethodBuilder`): Classes responsible for generating specific methods within the controllers (like CRUD operations and relationship management).
* **Injectors** (e.g., `ReturnStatementInjector`, `AuthenticationInjector`): Classes that inject specific code snippets or logic (like return statements with wrappers or security checks) into generated methods.
* **Utilities** (e.g., `RestnessUtil`, `WordInflector`): Helper classes for common tasks like name normalization and string manipulation during code generation.
