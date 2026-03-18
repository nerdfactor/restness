# RESTness Data

This module defines interfaces and provides default implementations for data access patterns required by the generated REST controllers. It aims to abstract the data layer interactions required for RESTness controllers, allowing users to focus on the business logic and data representation without worrying about the underlying data access mechanisms.

Key components include:

* **`DataAccessor`**: An interface for basic CRUD operations, allowing integration with different data access strategies (e.g., Repositories, Services).
* **`DataMapper`**: An interface for mapping between entity objects and Data Transfer Objects (DTOs). Includes a fallback `RestnessEntityMapper`.
* **`DataMerger`**: An interface for merging updates from DTOs into existing entity objects. Includes a fallback `RestnessEntityMerger` that supports both standard POJOs (modifying the original) and Java Records (creating a new instance via the canonical constructor).
* **`DataWrapper`**: An interface for wrapping responses, potentially adding metadata. Includes a default `RestnessEntityWrapper`.

Provides default Spring beans for `DataMapper` and `DataMerger` if no custom implementations are found in the user's application context.

The module is intended to be used in conjunction with the RESTness Core module, which generates the actual REST controller code or as a standalone library for data access patterns in Java applications.
