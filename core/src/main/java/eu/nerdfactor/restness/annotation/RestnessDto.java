package eu.nerdfactor.restness.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RestnessDto {

	Class<?> value();

	Class<?> list() default Object.class;

	Class<?> request() default Object.class;
}
