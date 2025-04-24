# RESTness Generator

This module contains the primary logic for generating the REST controller source code based on the configurations.

Key responsibilities include:

* Orchestrating the code generation process using builders and injectors from the `core` module.
* Potentially handling the export and import of RESTness configurations (e.g., using `ConfigMapper` for JSON/YAML serialization).
* Translating the abstract configuration models into concrete Java code using JavaPoet.
