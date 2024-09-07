package eu.nerdfactor.restness.code.injector;

public interface Injectable<T> {

	T inject(T builder);
}
