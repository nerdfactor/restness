package eu.nerdfactor.restness.entity;

import java.util.List;

/**
 * A test Record for verifying Record merge support in RestnessEntityMerger.
 */
public record ExampleRecord(String name, int value, boolean active, Long amount, List<String> tags) {
}
