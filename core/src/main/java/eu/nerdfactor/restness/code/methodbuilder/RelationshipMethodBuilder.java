package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.config.RelationConfiguration;
import eu.nerdfactor.restness.config.RelationType;

/**
 * A builder that orchestrates the creation of methods for managing entity relationships
 * within a REST controller class.
 * Relationship methods consist of:
 * <ul>
 *   <li>A method to read every relationship</li>
 *   <li>A method to set or add every relationship</li>
 *   <li>A method to delete every relationship</li>
 * </ul>
 *
 * @author Daniel Klug
 */
public class RelationshipMethodBuilder extends MultiStepMethodBuilder {

	/**
	 * Creates a new instance of {@link RelationshipMethodBuilder}.
	 *
	 * @return A new, unconfigured {@link RelationshipMethodBuilder}.
	 */
	public static RelationshipMethodBuilder create() {
		return new RelationshipMethodBuilder();
	}

	/**
	 * Builds the relationship management methods based on the configured relations
	 * and adds them to the provided {@link TypeSpec.Builder}.
	 * <p>
	 * It first checks if relations are enabled and configured. If so, it iterates
	 * through each {@link RelationConfiguration}. Depending on whether the relation
	 * type is {@link RelationType#SINGLE} or {@link RelationType#MULTIPLE}, it adds
	 * the corresponding specialized method builders (e.g., {@link GetSingleRelationMethodBuilder},
	 * {@link SetSingleRelationMethodBuilder}, {@link DeleteSingleRelationMethodBuilder} for single;
	 * {@link GetMultipleRelationsMethodBuilder}, {@link AddToRelationsMethodBuilder},
	 * {@link DeleteFromRelationsMethodBuilder} for multiple) to its internal list.
	 * Finally, it executes all added builders to generate and add the methods to the
	 * controller class specification.
	 *
	 * @param builder An existing {@link TypeSpec.Builder} representing the controller class
	 *                to which the generated relationship methods should be added.
	 * @return The {@link TypeSpec.Builder} updated with the newly added relationship methods,
	 * or the original builder if no relations are configured.
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		if (!configuration.isUsingRelations() || configuration.getRelationConfigurations() == null || configuration.getRelationConfigurations().isEmpty()) {
			return builder;
		}
		for (RelationConfiguration relation : configuration.getRelationConfigurations().values()) {
			if (relation.getRelationType() == RelationType.SINGLE) {
				this.and(GetSingleRelationMethodBuilder.create().withRelation(relation).withConfiguration(configuration).withInjectorRegistry(this.injectorRegistry));
				this.and(SetSingleRelationMethodBuilder.create().withRelation(relation).withConfiguration(configuration).withInjectorRegistry(this.injectorRegistry));
				this.and(DeleteSingleRelationMethodBuilder.create().withRelation(relation).withConfiguration(configuration).withInjectorRegistry(this.injectorRegistry));
			}
			if (relation.getRelationType() == RelationType.MULTIPLE) {
				this.and(GetMultipleRelationsMethodBuilder.create().withRelation(relation).withConfiguration(configuration).withInjectorRegistry(this.injectorRegistry));
				this.and(AddToRelationsMethodBuilder.create().withRelation(relation).withConfiguration(configuration).withInjectorRegistry(this.injectorRegistry));
				this.and(DeleteFromRelationsMethodBuilder.create().withRelation(relation).withConfiguration(configuration).withInjectorRegistry(this.injectorRegistry));
			}
		}
		this.buildAll(builder);
		return builder;
	}
}