package BTE.configuration.metaconfiguration.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Zmena mapovania elementu na atribut.
 * @author Milan
 */
@MapsTo(name="attribute")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Attribute {
}
