package dev.codeclub.hillock.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD}) // Adnotacja dla metod kontrolera
@Retention(RetentionPolicy.RUNTIME) // Dostępna w czasie działania
public @interface NoAuth {
}
