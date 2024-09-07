package eu.nerdfactor.restness.config;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import eu.nerdfactor.restness.annotation.IdAccessor;
import eu.nerdfactor.restness.annotation.Relation;
import eu.nerdfactor.restness.annotation.RelationAccessor;
import eu.nerdfactor.restness.util.GeneratedRestUtil;
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
 * Builder that creates a relation configuration from the
 * provided information.
 *
 * @author Daniel Klug
 */
public class RelationConfigurationBuilder {

	private Elements elementUtils;

	/**
	 * The element for which the relations should be collected.
	 */
	private Element element;

	/**
	 * All classes that get compiled.
	 */
	private Map<String, List<TypeName>> classes;

	private boolean withDtos = true;

	public RelationConfigurationBuilder withElement(Element element) {
		this.element = element;
		return this;
	}

	public RelationConfigurationBuilder withUtils(Elements elementUtils) {
		this.elementUtils = elementUtils;
		return this;
	}

	public RelationConfigurationBuilder withClasses(Map<String, List<TypeName>> classes) {
		this.classes = classes;
		return this;
	}

	public RelationConfigurationBuilder withDtos(boolean dtos) {
		this.withDtos = dtos;
		return this;
	}

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
					relation.setName(field.getSimpleName().toString());
					relation.setEntityClass(fieldClass);
					relation.setDtoClass(this.withDtos ? this.findDtoType(fieldClass, this.classes) : null);
					relation.setIdClass(idClass);
					relation.setIdAccessor(idAccessor);
					relation.setType(this.findRelationType(relationType, elementValues));
					relation.setAccessors(this.findRelationAccessors(field.getSimpleName().toString(), fieldClass, entityElement, elementValues));
					relations.put(field.getSimpleName().toString(), relation);
				} else if (!relations.containsKey(field.getSimpleName().toString()) && (annotationName.equals(OneToMany.class.getName()) || annotationName.equals(ManyToMany.class.getName()) || annotationName.equals(ManyToOne.class.getName()))) {
					final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = this.elementUtils.getElementValuesWithDefaults(annotation);
					RelationConfiguration relation = new RelationConfiguration();
					relation.setName(field.getSimpleName().toString());
					relation.setEntityClass(fieldClass);
					relation.setDtoClass(this.withDtos ? this.findDtoType(fieldClass, this.classes) : null);
					relation.setIdClass(idClass);
					relation.setIdAccessor(idAccessor);
					relation.setType(relationType);
					relation.setAccessors(this.findRelationAccessors(field.getSimpleName().toString(), fieldClass, entityElement, elementValues));
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
	 * @return The original relation type or the new relation type of the annotation.
	 */
	private RelationType findRelationType(RelationType relationType, Map<? extends ExecutableElement, ? extends AnnotationValue> annotationValues) {
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
	 * Auto discover Dto Type for the type by checking all current classes for the same name with a Dto Suffix.
	 *
	 * @param typeName The class for which the Dto should be found.
	 * @param classes  A map of all possible Dto classes.
	 * @return The found Dto type or the original class type, if none was found.
	 */
	private TypeName findDtoType(ClassName typeName, Map<String, List<TypeName>> classes) {
		String entityClassName = typeName.toString();
		entityClassName = entityClassName.substring(entityClassName.lastIndexOf('.') + 1).trim();
		entityClassName = GeneratedRestUtil.normalizeEntityName(entityClassName);
		String dtoClassName = entityClassName + "Dto";
		if (classes.containsKey(dtoClassName) && classes.get(dtoClassName).size() == 1) {
			return (classes.get(dtoClassName).get(0));
		}
		return typeName;
	}


	private String[] findRelationAccessors(String relationName, ClassName relationEntity, Element relationElement, Map<? extends ExecutableElement, ? extends AnnotationValue> annotationValues) {
		// Create accessors with default names based on the relation name and entity.
		String methodSuffix = relationName.substring(0, 1).toUpperCase() + relationName.substring(1);
		String methodEntitySuffix = GeneratedRestUtil.removeEnd(methodSuffix, "s");
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
