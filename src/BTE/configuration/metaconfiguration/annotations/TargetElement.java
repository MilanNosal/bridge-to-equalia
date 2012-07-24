package BTE.configuration.metaconfiguration.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metaanotacia na definovanie sposobu mapovania cielovho elementu zdr. kodu.
 * @author Milan
 */
@MapsTo(name="targetElement")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
public @interface TargetElement {
    /**
     * Predvolene sa zmena cieloveho elementu aplikuje na polozku, na ktorej
     * sa anotacia nachadza, tymto je mozne urcit na co presne sa to viaze.
     * @return
     */
    String targetConfiguration() default "";

    /**
     * Nazov elementu/atributu, na ktory sa ma mapovat cielovy element kodu.
     * @return
     */
    String name();

    /**
     * Priznak, ci sa maju pouzit genericke nazvy, tj. podla typu elementu zdr.
     * kodu. Ak je true, name sa neberie v uvahu.
     * @return
     */
    boolean generic() default false;

    /**
     * Priznak, ci sa ma informacia o cielovom jazykovom elemente mapovat na
     * element, predvolene sa mapuje na atribut.
     * @return
     */
    boolean element() default false;

    enum PrintType {
        FULL, CONTEXT, SIMPLE, NO_PRINT
    }

    /**
     * Typ vypisu informacie o elemente zdr. kodu. NO_PRINT zrusi mapovanie.
     * @return
     */
    PrintType printType() default PrintType.CONTEXT;
}
