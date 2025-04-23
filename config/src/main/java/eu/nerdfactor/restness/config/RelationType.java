package eu.nerdfactor.restness.config;

/**
 * The type of relation between entities.
 *
 * @author Daniel Klug
 */
public enum RelationType {

	SINGLE,     // Relation to a single entity.
	MULTIPLE,   // Relation to a collection of entities.
	REFLECT     // Type of relation is determined by reflection.
}
