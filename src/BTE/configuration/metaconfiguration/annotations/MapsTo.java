package BTE.configuration.metaconfiguration.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metainformacia o mapovani mien a nazvov v XML a anotaciach. Meni predvolene
 * spravanie, pri ktorom sa mapuju nazvy v XML na nazvy anotacnych typov a
 * vlastnosti.
 * @author Milan
 */
@MapsTo(name="mapsTo")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
public @interface MapsTo {
    /**
     * Nazov elementu/atributu, na ktory sa ma an. typ, resp. vlastnost mapovat.
     * @return
     */
    String name();

    /**
     * Nazov typu v XSD.
     * @return
     */
    String typeName() default "${name}-type";
}
