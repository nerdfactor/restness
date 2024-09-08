# RESTness

A simple library to generate REST controllers for a spring boot application.

Other than [spring-data-rest](https://github.com/spring-projects/spring-data-rest) the new controllers
will be generated as java code during compile time and compiled into
your project instead of created during runtime. This allows you to
"look under the hood" of the controllers and modify them if necessary.

## Features

* Creates controllers for a simple REST Api.
* Exposes entities and their relations to other entities.
* Allows for [pagination](https://docs.spring.io/spring-data/rest/docs/current/reference/html/#paging-and-sorting) and [dynamic filtering](https://github.com/turkraft/spring-filter).
* Supports validation of incoming data.
* Can restrict access to endpoints with spring-security.
* Allows to map entities to data transfer objects.

## Getting Started

Add a simple annotation to an empty controller for your entity:

```java

/**
 * Base controller for Products.<br>
 * Uses RestnessController to configure a generated controller based on the controller.
 * Provides information about the entity and id to use in the generated controller.
 */
@RestnessController(value = "/api/products", entity = Product.class, id = Integer.class)
public class ProductController {
}
```

and build your project. The generated class will look like this

```java

@RestController
public class RestnessProductController {

  @GetMapping("/api/products")
  public ResponseEntity<List<Product>> all() {
    // [..]
  }

  @GetMapping("/api/products/{id}")
  public ResponseEntity<Product> get(@PathVariable final Integer id) {
    // [..]
  }

  @PostMapping("/api/products")
  public ResponseEntity<Product> create(
          @RequestBody @Valid Product dto) {
    // [..]
  }

  @PutMapping("/api/products/{id}")
  public ResponseEntity<Product> set(@PathVariable final Integer id,
                                     @RequestBody @Valid Product dto) {
    // [..]
  }

  @PatchMapping("/api/products/{id}")
  public ResponseEntity<Product> update(@PathVariable final Integer id,
                                        @RequestBody @Valid Product dto) {
    // [..]
  }

  @DeleteMapping("/api/products/{id}")
  public ResponseEntity<Product> delete(@PathVariable final Integer id) {
    // [..]
  }

  // [more methods to access relationships]
}
```

and exposes basic REST methods like list, get, create, replace, update and delete entities and its relationships.

## Important Notice

This is a very small and simple project I created because I could not
get spring-data-rest to work in the way I wanted (Support for
spring-security, dto mapping, complex searching and usage of services
instead of direct access to repositories). Therefore, the generated
classes are the way I like them and contain only the features I needed.
Please use spring-data-rest instead for a full-fledged and super
robust REST library.

## License

Distributed under the [MIT license](LICENSE.md).