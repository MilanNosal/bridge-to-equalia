package BTE.configuration.metaconfiguration.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Oznacenie informacie, ktora sa ma presunut niekam do hierarchie MM. Sluzi na
 * modelovanie hierarchie z anotacnych typov a ich vlastnosti do XML.
 * @author Milan
 */
@MapsTo(name="inside")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
public @interface Inside {
    /**
     * Meno konfiguracneho typu pri XML mapovani.
     * @return
     */
    String parent();

    /**
     * Priznak, ci bola informacia spracovana, ten potrebuje procesor. Pre
     * pouzivatela bezpredmetne.
     */
    boolean processed() default false;

    enum Level {
        SAME, LOWER, HIGHER, SAME_LOWER, SAME_HIGHER
    }

    /**
     * Uroven v hierarchii zdr. kodov, na ktorej sa vzhladom na rodica maju
     * hladat vyskyty tejto informacie.
     * @return
     */
    Level level() default Level.SAME_LOWER;

    /**
     * Priznak, ci ma byt informacia nastavena na spajanie, t.j. ci sa maju pri
     * pri spajani na tejto urovni hierarchie konf. informacii riesit
     * konflikty.
     * @return
     */
    boolean setMergingPoint() default false;
}
