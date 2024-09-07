package eu.nerdfactor.restness.config;

/**
 * The type of access a method provides.
 *
 * @author Daniel Klug
 */
public enum AccessorType {

	GET,    // Get some data.
	SET,    // Set some data.
	ADD,    // Add some data to a collection.
	REMOVE  // Remove some data from a collection.
}
