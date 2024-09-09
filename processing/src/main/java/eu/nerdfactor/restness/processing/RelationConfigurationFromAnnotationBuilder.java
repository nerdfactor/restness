package eu.nerdfactor.restness.processing;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.restness.annotation.IdAccessor;
import eu.nerdfactor.restness.annotation.Relation;
import eu.nerdfactor.restness.annotation.RelationAccessor;
import eu.nerdfactor.restness.config.AccessorType;
import eu.nerdfactor.restness.config.RelationConfiguration;
import eu.nerdfactor.restness.config.RelationType;
import eu.nerdfactor.restness.util.RestnessUtil;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import java.util.*;

import static javax.lang.model.util.ElementFilter.fieldsIn;
import static javax.lang.model.util.ElementFilter.methodsIn;

/**
 * Builder that creates a relation configuration from annotations.
 *
 * @author Daniel Klug
 */
public class RelationConfigurationFromAnnotationBuilder {

	/**
	 * The element utilities during annotation processing.
	 */
	protected Elements elementUtils;

	/**
	 * The element for which the relations should be collected.
	 */
	protected Element element;

	/**
	 * A map of possible Dto Classes.
	 */
	@Deprecated(since = "0.0.20")
	protected Map<String, List<TypeName>> dtoClasses;

	/**
	 * If the relations use Dtos.
	 */
	@Deprecated(since = "0.0.20")
	protected boolean withDtos = true;

	/**
	 * @param element The annotated Element.
	 * @return The builder in a fluent api pattern.
	 */
	public RelationConfigurationFromAnnotationBuilder withElement(Element element) {
		this.element = element;
		return this;
	}

	/**
	 * @param utils The element utilities during annotation processing.
	 * @return The builder in a fluent api pattern.
	 */
	public RelationConfigurationFromAnnotationBuilder withUtils(Elements utils) {
		this.elementUtils = utils;
		return this;
	}

	/**
	 * @param dtoClasses A map of possible Dto Classes.
	 * @return The builder in a fluent api pattern.
	 */
	@Deprecated(since = "0.0.20")
	public RelationConfigurationFromAnnotationBuilder withClasses(Map<String, List<TypeName>> dtoClasses) {
		this.dtoClasses = dtoClasses;
		return this;
	}

	/**
	 * @param dtos True if the builder should use Dtos.
	 * @return The builder in a fluent api pattern.
	 */
	@Deprecated(since = "0.0.20")
	public RelationConfigurationFromAnnotationBuilder withDtos(boolean dtos) {
		this.withDtos = dtos;
		return this;
	}

	public static RelationConfigurationFromAnnotationBuilder create(){
		return new RelationConfigurationFromAnnotationBuilder();
	}

	/**
	 * Collect information about the relations from the annotated class.
	 *
	 * @return A map with all found {@link RelationConfiguration}s.
	 */
	public Map<String, RelationConfiguration> build() {
		Map<String, RelationConfiguration> relations = new HashMap<>();
		// Check every field for annotations describing a relation or the id.
		for (VariableElement field : fieldsIn(this.element.getEnclosedElements())) {
			RelationType relationType = RelationType.SINGLE;
			TypeName typeName = ParameterizedTypeName.get(field.asType());
			// Check if the type is a collection and use the first type argument
			// as the relation type.
			if (typeName instanceof ParameterizedTypeName parameterizedTypeName) {
				if (!parameterizedTypeName.typeArguments.isEmpty() &&
						(parameterizedTypeName.rawType.equals(TypeName.get(List.class))
								|| parameterizedTypeName.rawType.equals(TypeName.get(Set.class))
								|| parameterizedTypeName.rawType.equals(TypeName.get(Collection.class)))
						|| parameterizedTypeName.rawType.equals(TypeName.get(Iterable.class))) {
					typeName = parameterizedTypeName.typeArguments.get(0);
					relationType = RelationType.MULTIPLE;
				}
			}
			if (typeName.isPrimitive()) {
				typeName = typeName.box();
			}
			ClassName fieldClass = ClassName.bestGuess(typeName.toString());

			TypeName idClass = TypeName.get(Integer.class);
			String idName = "id";
			String idAccessor = "getId";
			TypeElement entityElement = this.elementUtils.getTypeElement(fieldClass.canonicalName());
			if (entityElement != null) {
				// Check the fields of the entity for an Id annotation and use it as the type.
				for (VariableElement entityField : fieldsIn(entityElement.getEnclosedElements())) {
					TypeName tempIdClass = TypeName.get(entityField.asType());
					for (AnnotationMirror annotation : entityField.getAnnotationMirrors()) {
						String annotationName = annotation.getAnnotationType().toString();
						if (annotationName.equals(Id.class.getName())) {
							idClass = tempIdClass;
							if (!entityField.getSimpleName().toString().equalsIgnoreCase(idName)) {
								// If the id is not called "id", rename the accessor to the field name.
								idName = entityField.getSimpleName().toString();
								idAccessor = "get" + idName.substring(0, 1).toUpperCase() + idName.substring(1);
							}
							break;
						}
					}
				}
				// Check the methods of the entity for an IdAccessor annotation and change the accessor.
				for (ExecutableElement method : methodsIn(entityElement.getEnclosedElements())) {
					for (AnnotationMirror annotation : method.getAnnotationMirrors()) {
						String annotationName = annotation.getAnnotationType().toString();
						if (annotationName.equals(IdAccessor.class.getName())) {
							final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = this.elementUtils.getElementValuesWithDefaults(annotation);
							for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
								try {
									String name = entry.getKey().getSimpleName().toString();
									String value = entry.getValue().getValue().toString();
									if (name.equals("name") && value.equals(idName)) {
										idAccessor = method.getSimpleName().toString();
										break;
									}
								} catch (Exception e) {
									// ignore the exception
								}
							}
						}
					}
				}
			}

			for (AnnotationMirror annotation : field.getAnnotationMirrors()) {
				String annotationName = annotation.getAnnotationType().toString();
				if (annotationName.equals(Relation.class.getName())) {
					final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = this.elementUtils.getElementValuesWithDefaults(annotation);
					RelationConfiguration relation = new RelationConfiguration();
					relation.setRelationName(field.getSimpleName().toString());
					relation.setEntityClassName(fieldClass);
					relation.setResponseObjectClassName(this.withDtos ? this.findDtoType(fieldClass, this.dtoClasses) : null);
					relation.setIdClassName(idClass);
					relation.setIdAccessorMethodName(idAccessor);
					relation.setRelationType(this.findRelationType(relationType, elementValues));
					relation.setAccessorMethodNames(this.findRelationAccessors(field.getSimpleName().toString(), fieldClass, entityElement, elementValues));
					relations.put(field.getSimpleName().toString(), relation);
				} else if (!relations.containsKey(field.getSimpleName().toString()) && (annotationName.equals(OneToMany.class.getName()) || annotationName.equals(ManyToMany.class.getName()) || annotationName.equals(ManyToOne.class.getName()))) {
					final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = this.elementUtils.getElementValuesWithDefaults(annotation);
					RelationConfiguration relation = new RelationConfiguration();
					relation.setRelationName(field.getSimpleName().toString());
					relation.setEntityClassName(fieldClass);
					relation.setResponseObjectClassName(this.withDtos ? this.findDtoType(fieldClass, this.dtoClasses) : null);
					relation.setIdClassName(idClass);
					relation.setIdAccessorMethodName(idAccessor);
					relation.setRelationType(relationType);
					relation.setAccessorMethodNames(this.findRelationAccessors(field.getSimpleName().toString(), fieldClass, entityElement, elementValues));
					relations.put(field.getSimpleName().toString(), relation);
				}
			}
		}
		return relations;
	}

	/**
	 * Find the relation type from a Relation annotation.
	 *
	 * @param relationType     The possible relation type.
	 * @param annotationValues The values of the annotation.
	 * @return The original relation type or the new relation type of the
	 * annotation.
	 */
	protected RelationType findRelationType(RelationType relationType, Map<? extends ExecutableElement, ? extends AnnotationValue> annotationValues) {
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationValues.entrySet()) {
			try {
				String name = entry.getKey().getSimpleName().toString();
				String value = entry.getValue().getValue().toString();
				// If the relation type is REFLECT, the original type should be kept.
				if (name.equals("type") && RelationType.valueOf(value) != RelationType.REFLECT) {
					relationType = RelationType.valueOf(value);
					break;
				}
			} catch (Exception e) {
			}
		}
		return relationType;
	}

	/**
	 * Auto discover Dto Type for the type by checking all current classes for
	 * the same name with a Dto Suffix.
	 *
	 * @param typeName The class for which the Dto should be found.
	 * @param classes  A map of all possible Dto classes.
	 * @return The found Dto type or the original class type, if none was found.
	 */
	protected TypeName findDtoType(ClassName typeName, Map<String, List<TypeName>> classes) {
		String entityClassName = typeName.toString();
		entityClassName = entityClassName.substring(entityClassName.lastIndexOf('.') + 1).trim();
		entityClassName = RestnessUtil.normalizeEntityName(entityClassName);
		String dtoClassName = entityClassName + "Dto";
		if (classes.containsKey(dtoClassName) && classes.get(dtoClassName).size() == 1) {
			return (classes.get(dtoClassName).get(0));
		}
		return typeName;
	}


	protected String[] findRelationAccessors(String relationName, ClassName relationEntity, Element relationElement, Map<? extends ExecutableElement, ? extends AnnotationValue> annotationValues) {
		// Create accessors with default names based on the relation name and entity.
		String methodSuffix = relationName.substring(0, 1).toUpperCase() + relationName.substring(1);
		String methodEntitySuffix = RestnessUtil.removeEnd(methodSuffix, "s");
		String[] accessors = new String[]{"get" + methodSuffix, "set" + methodSuffix, "add" + methodEntitySuffix, "remove" + methodEntitySuffix};

		// If values from the annotation like Relation is passed as parameter, try to
		// get the accessor names from them.
		if (annotationValues != null && !annotationValues.isEmpty()) {
			annotationValues.forEach((executableElement, annotationValue) -> {
				try {
					String name = executableElement.getSimpleName().toString();
					String value = annotationValue.getValue().toString();
					switch (name) {
						case "get":
							accessors[0] = !value.isEmpty() ? value : accessors[0];
							break;
						case "set":
							accessors[1] = !value.isEmpty() ? value : accessors[1];
							break;
						case "add":
							accessors[2] = !value.isEmpty() ? value : accessors[2];
							break;
						case "remove":
							accessors[3] = !value.isEmpty() ? value : accessors[3];
							break;
					}
				} catch (Exception e) {
				}
			});
		}

		// Check every method of the relation entity for annotations specifying access to the relation.
		for (ExecutableElement method : methodsIn(relationElement.getEnclosedElements())) {
			for (AnnotationMirror annotation : method.getAnnotationMirrors()) {
				String annotationName = annotation.getAnnotationType().toString();
				if (annotationName.equals(RelationAccessor.class.getName())) {
					String accessedRelationName = "";
					final List<AccessorType> relationTypes = new ArrayList<>();
					final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = this.elementUtils.getElementValuesWithDefaults(annotation);
					for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
						try {
							String name = entry.getKey().getSimpleName().toString();
							if (name.equals("name")) {
								accessedRelationName = entry.getValue().getValue().toString();
							}
							if (name.equals("type")) {
								((List<?>) entry.getValue().getValue()).forEach(o -> {
									String enumName = o.toString().substring(o.toString().lastIndexOf('.') + 1).trim();
									relationTypes.add(AccessorType.valueOf(enumName));
								});
								int i = 0;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (accessedRelationName.equals(relationName)) {
						for (AccessorType access : relationTypes) {
							switch (access) {
								case GET:
									accessors[0] = method.getSimpleName().toString();
									break;
								case SET:
									accessors[1] = method.getSimpleName().toString();
									break;
								case ADD:
									accessors[2] = method.getSimpleName().toString();
									break;
								case REMOVE:
									accessors[3] = method.getSimpleName().toString();
									break;
							}
						}
					}
				}
			}
		}
		return accessors;
	}
}
