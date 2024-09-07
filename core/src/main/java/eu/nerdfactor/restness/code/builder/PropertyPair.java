package eu.nerdfactor.restness.code.builder;

import com.squareup.javapoet.TypeName;

/**
 * A pair of name and type.
 *
 * @param name The name of the pair.
 * @param type The type of the pair.
 */
public record PropertyPair(String name, TypeName type) {
}
