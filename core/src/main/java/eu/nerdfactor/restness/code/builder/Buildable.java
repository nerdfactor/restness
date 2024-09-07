package eu.nerdfactor.restness.code.builder;

public interface Buildable<T> {

	T build(T builder);
}
