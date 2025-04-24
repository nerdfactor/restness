package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.TypeSpec;

/**
 * A builder that orchestrates the creation of standard CRUD (Create, Read, Update, Delete)
 * methods within a REST controller class.
 * CRUD methods consist of:
 * <ul>
 *  <li>A method to create an entity.</li>
 *  <li>A method to read an entity.</li>
 *  <li>A method to update an entity.</li>
 *  <li>A method to set an entity.</li>
 *  <li>A method to delete an entity<./li>
 *  <li>A method to delete an entity by its id.</li>
 * </ul>
 *
 * @author Daniel Klug
 */
public class CrudMethodBuilder extends MultiStepMethodBuilder {

	/**
	 * Creates a new instance of {@link CrudMethodBuilder}.
	 *
	 * @return A new, unconfigured {@link CrudMethodBuilder}.
	 */
	public static CrudMethodBuilder create() {
		return new CrudMethodBuilder();
	}

	/**
	 * Builds all standard CRUD methods (Create, Read, Update, Set, Delete) and adds them
	 * to the provided {@link TypeSpec.Builder}.
	 * It configures each individual method builder (Create, Read, Update, Set, Delete)
	 * with the stored {@link #configuration} and then executes them sequentially
	 * to add the generated methods to the controller class specification.
	 *
	 * @param builder An existing {@link TypeSpec.Builder} representing the controller class
	 *                to which the generated CRUD methods should be added.
	 * @return The {@link TypeSpec.Builder} updated with the newly added CRUD methods.
	 */
	@Override
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
