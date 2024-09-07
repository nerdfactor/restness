package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.config.RelationConfiguration;
import eu.nerdfactor.restness.config.RelationType;

/**
 * A builder that can be used to create a methods to manage relationships in a
 * controller.
 * <p>
 * Relationship methods consist of:
 * <li>A method to read every relationship</li>
 * <li>A method to set or add every relationship</li>
 * <li>A method to delete every relationship</li>
 *
 * @author Daniel Klug
 */
public class RelationshipMethodBuilder extends MultiStepMethodBuilder {

	/**
	 * Create a new {@link RelationshipMethodBuilder}.
	 *
	 * @return A new {@link RelationshipMethodBuilder}.
	 */
	public static RelationshipMethodBuilder create() {
		return new RelationshipMethodBuilder();
	}

	/**
	 * Create a {@link TypeSpec.Builder} containing a methods to manage
	 * relationships.
	 *
	 * @param builder An existing builder object that will be used.
	 * @return The build {@link TypeSpec.Builder}.
	 */
	@Override
	public TypeSpec.Builder buildWith(TypeSpec.Builder builder) {
		if (!configuration.isUsingRelations() || configuration.getRelationConfigurations() == null || configuration.getRelationConfigurations().isEmpty()) {
			return builder;
		}
		for (RelationConfiguration relation : configuration.getRelationConfigurations().values()) {
			if (relation.getRelationType() == RelationType.SINGLE) {
				this.and(new GetSingleRelationMethodBuilder().withRelation(relation).withConfiguration(configuration));
				this.and(new SetSingleRelationMethodBuilder().withRelation(relation).withConfiguration(configuration));
				this.and(new DeleteSingleRelationMethodBuilder().withRelation(relation).withConfiguration(configuration));
			}
			if (relation.getRelationType() == RelationType.MULTIPLE) {
				this.and(new GetMultipleRelationsMethodBuilder().withRelation(relation).withConfiguration(configuration));
				this.and(new AddToRelationsMethodBuilder().withRelation(relation).withConfiguration(configuration));
				this.and(new DeleteFromRelationsMethodBuilder().withRelation(relation).withConfiguration(configuration));
			}
		}
		this.buildAll(builder);
		return builder;
	}
}