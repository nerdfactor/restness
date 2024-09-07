package eu.nerdfactor.restness.code.builder;

public interface Injectable<T> {

	T inject(T builder);
}
