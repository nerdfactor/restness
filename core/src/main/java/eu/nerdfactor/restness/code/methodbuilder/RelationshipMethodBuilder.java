package eu.nerdfactor.restness.code.methodbuilder;

import com.squareup.javapoet.TypeSpec;
import eu.nerdfactor.restness.code.builder.Buildable;
import eu.nerdfactor.restness.code.builder.Configurable;
import eu.nerdfactor.restness.code.builder.MultiStepBuilder;
import eu.nerdfactor.restness.config.ControllerConfiguration;
import eu.nerdfactor.restness.config.RelationConfiguration;
import eu.nerdfactor.restness.config.RelationType;
import org.jetbrains.annotations.NotNull;

public class RelationshipMethodBuilder extends MultiStepBuilder<TypeSpec.Builder> implements Configurable<ControllerConfiguration>, Buildable<TypeSpec.Builder> {

	protected ControllerConfiguration configuration;

	@Override
	public RelationshipMethodBuilder withConfiguration(@NotNull ControllerConfiguration configuration) {
		this.configuration = configuration;
		return this;
	}

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
		this.steps.forEach(buildStep -> buildStep.buildWith(builder));
		return builder;
	}
}