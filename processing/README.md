# RESTness Processing

This module contains the Java Annotation Processor (`RestnessAnnotationProcessor`) responsible for gathering the configuration and generation of the REST controller at compile time.

Key functions:

* Registers itself to process RESTness annotations (`@RestnessConfiguration`, `@RestnessController`, `@RestnessSecurity`).
* Scans the annotated elements during the Maven build process.
* Builds internal configuration models based on the extracted annotation data.
* Triggers the code generation process, by invoking the configured components from the `generator` module.
* Writes the generated Java source files.
