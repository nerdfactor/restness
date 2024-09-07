package eu.nerdfactor.restness.code.builder;

import org.jetbrains.annotations.NotNull;

public interface Configurable<T> {

	Configurable<T> withConfiguration(@NotNull T configuration);
}
