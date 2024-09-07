package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.TypeSpec;

/**
 * A builder that can be used to create a CRUD methods in a controller. It will
 * use a set of additional builders that build each method.
 * <p>
 * CRUD methods consist of:
 * <li>A method to create an entity.</li>
 * <li>A method to read an entity.</li>
 * <li>A method to update an entity.</li>
 * <li>A method to set an entity.</li>
 * <li>A method to delete an entity<./li>
 * <li>A method to delete an entity by its id.</li>
 *
 * @author Daniel Klug
 */
public class CrudMethodBuilder extends MultiStepMethodBuilder {

	/**
	 * Create a new {@link CrudMethodBuilder}.
	 *
	 * @return A new {@link CrudMethodBuilder}.
	 */
	public static CrudMethodBuilder create() {
		return new CrudMethodBuilder();
	}

	/**
	 * Create a {@link TypeSpec.Builder} containing CRUD methods.
	 *
	 * @param builder An existing builder object that will be used.
	 * @return The build {@link TypeSpec.Builder}.
	 */
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		this.and(CreateEntityMethodBuilder.create().withConfiguration(this.configuration));
		this.and(ReadEntityMethodBuilder.create().withConfiguration(this.configuration));
		this.and(UpdateEntityMethodBuilder.create().withConfiguration(this.configuration));
		this.and(SetEntityMethodBuilder.create().withConfiguration(this.configuration));
		this.and(DeleteEntityMethodBuilder.create().withConfiguration(this.configuration));
		this.buildAll(builder);
		return builder;
	}
}
