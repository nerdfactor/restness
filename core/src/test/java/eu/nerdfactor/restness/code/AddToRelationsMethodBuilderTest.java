package eu.nerdfactor.restness.code;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.methodbuilder.AddToRelationsMethodBuilder;
import eu.nerdfactor.restness.code.methodbuilder.DeleteFromRelationsMethodBuilder;
import eu.nerdfactor.restness.code.methodbuilder.GetMultipleRelationsMethodBuilder;
import eu.nerdfactor.restness.entity.Example;
import eu.nerdfactor.restness.entity.Related;
import eu.nerdfactor.restness.entity.RelatedDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;

@ExtendWith(MockitoExtension.class)
class AddToRelationsMethodBuilderTest {

	@Test
	void shouldCreateAddToRelationsMethod() {
		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		AddToRelationsMethodBuilder.create()
				.withRequestMappings(new ArrayList<>())
				.withBasePath("/api/example")
				.withIdType(ClassName.get(Integer.class))
				.withEntityType(ClassName.get(Example.class))
				.withSecurityConfig(null)
				.withResponseWrapperType(TypeName.OBJECT)
				.withRelationName("items")
				.withUsingDto(false)
				.withRelationResponseType(null)
				.withRelationEntityType(ClassName.get(Related.class))
				.withRelationIdType(ClassName.get(Integer.class))
				.withRelationIdAccessor("getId")
				.withRelationAdder("addItem")
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();

		// Should contain both the body-based and id-based add methods
		Assertions.assertTrue(code.contains("addItem("));
		Assertions.assertTrue(code.contains("/api/example/{id}/items"));
		Assertions.assertTrue(code.contains("addItemById"));
		Assertions.assertTrue(code.contains("/api/example/{id}/items/{relationId}"));
		Assertions.assertTrue(code.contains("this.dataAccessor.readData(id)"));
		Assertions.assertTrue(code.contains("this.dataAccessor.updateData(entity)"));
	}

	@Test
	void shouldCreateAddToRelationsMethodWithDto() {
		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		AddToRelationsMethodBuilder.create()
				.withRequestMappings(new ArrayList<>())
				.withBasePath("/api/example")
				.withIdType(ClassName.get(Integer.class))
				.withEntityType(ClassName.get(Example.class))
				.withSecurityConfig(null)
				.withResponseWrapperType(TypeName.OBJECT)
				.withRelationName("items")
				.withUsingDto(true)
				.withRelationResponseType(ClassName.get(RelatedDto.class))
				.withRelationEntityType(ClassName.get(Related.class))
				.withRelationIdType(ClassName.get(Integer.class))
				.withRelationIdAccessor("getId")
				.withRelationAdder("addItem")
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();

		// When using DTO, the parameter type should be the DTO type
		Assertions.assertTrue(code.contains("RelatedDto dto"));
		Assertions.assertTrue(code.contains("ResponseEntity<List<RelatedDto>>"));
	}

	@Test
	void shouldSkipExistingRequest() {
		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		ArrayList<String> existingMappings = new ArrayList<>();
		existingMappings.add("POST/api/example/{id}/items");
		existingMappings.add("PUT/api/example/{id}/items");
		existingMappings.add("PATCH/api/example/{id}/items");

		AddToRelationsMethodBuilder.create()
				.withRequestMappings(existingMappings)
				.withBasePath("/api/example")
				.withIdType(ClassName.get(Integer.class))
				.withEntityType(ClassName.get(Example.class))
				.withSecurityConfig(null)
				.withResponseWrapperType(TypeName.OBJECT)
				.withRelationName("items")
				.withUsingDto(false)
				.withRelationResponseType(null)
				.withRelationEntityType(ClassName.get(Related.class))
				.withRelationIdType(ClassName.get(Integer.class))
				.withRelationIdAccessor("getId")
				.withRelationAdder("addItem")
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();

		// The body-based method should be skipped but the ID-based should still exist
		Assertions.assertFalse(code.contains("public ResponseEntity<List<Related>> addItem("));
		Assertions.assertTrue(code.contains("addItemById"));
	}

	@Test
	void shouldCreateGetMultipleRelationsMethod() {
		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		GetMultipleRelationsMethodBuilder.create()
				.withRequestMappings(new ArrayList<>())
				.withBasePath("/api/example")
				.withIdType(ClassName.get(Integer.class))
				.withEntityType(ClassName.get(Example.class))
				.withSecurityConfig(null)
				.withResponseWrapperType(TypeName.OBJECT)
				.withRelationName("items")
				.withUsingDto(false)
				.withRelationResponseType(null)
				.withRelationEntityType(ClassName.get(Related.class))
				.withRelationGetter("getItems")
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();

		Assertions.assertTrue(code.contains("getItems"));
		Assertions.assertTrue(code.contains("@GetMapping"));
		Assertions.assertTrue(code.contains("/api/example/{id}/items"));
		Assertions.assertTrue(code.contains("this.dataAccessor.readData(id)"));
		Assertions.assertTrue(code.contains("responseList.add(response)"));
	}

	@Test
	void shouldCreateGetMultipleRelationsMethodWithDto() {
		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		GetMultipleRelationsMethodBuilder.create()
				.withRequestMappings(new ArrayList<>())
				.withBasePath("/api/example")
				.withIdType(ClassName.get(Integer.class))
				.withEntityType(ClassName.get(Example.class))
				.withSecurityConfig(null)
				.withResponseWrapperType(TypeName.OBJECT)
				.withRelationName("items")
				.withUsingDto(true)
				.withRelationResponseType(ClassName.get(RelatedDto.class))
				.withRelationEntityType(ClassName.get(Related.class))
				.withRelationGetter("getItems")
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();

		Assertions.assertTrue(code.contains("this.dataMapper.map(rel, RelatedDto.class)"));
		Assertions.assertTrue(code.contains("ResponseEntity<List<RelatedDto>>"));
	}

	@Test
	void shouldCreateDeleteFromRelationsMethod() {
		TypeSpec.Builder builder = TypeSpec.classBuilder("ExampleController")
				.addAnnotation(RestController.class)
				.addModifiers(Modifier.PUBLIC);

		DeleteFromRelationsMethodBuilder.create()
				.withRequestMappings(new ArrayList<>())
				.withBasePath("/api/example")
				.withIdType(ClassName.get(Integer.class))
				.withEntityType(ClassName.get(Example.class))
				.withSecurityConfig(null)
				.withResponseWrapperType(TypeName.OBJECT)
				.withRelationName("items")
				.withUsingDto(false)
				.withRelationResponseType(null)
				.withRelationEntityType(ClassName.get(Related.class))
				.withRelationIdType(ClassName.get(Integer.class))
				.withRelationIdAccessor("getId")
				.withRelationRemover("removeItem")
				.buildWith(builder);

		String code = JavaFile.builder("eu.nerdfactor.test", builder.build()).build().toString();

		Assertions.assertTrue(code.contains("removeItem("));
		Assertions.assertTrue(code.contains("@DeleteMapping"));
		Assertions.assertTrue(code.contains("/api/example/{id}/items/{relationId}"));
		Assertions.assertTrue(code.contains("removeItemById"));
		Assertions.assertTrue(code.contains("this.dataAccessor.readData(id)"));
		Assertions.assertTrue(code.contains("this.dataAccessor.updateData(entity)"));
	}
}
